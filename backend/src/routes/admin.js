const express = require('express');
const router = express.Router();
const { supabaseAdmin } = require('../config/supabase');

// Admin: Get all projects with stats
router.get('/projects', async (req, res) => {
  try {
    const { data: projects, error: projectsError } = await supabaseAdmin
      .from('projects')
      .select(`
        *,
        user_profiles(
          full_name,
          organization_name,
          email
        ),
        carbon_credits(
          id,
          quantity,
          status,
          total_value
        ),
        monitoring_data(
          id,
          verification_status
        )
      `)
      .order('created_at', { ascending: false });

    if (projectsError) throw projectsError;

    const projectsWithStats = projects.map(project => ({
      ...project,
      stats: {
        totalCredits: project.carbon_credits?.reduce((sum, credit) => sum + parseFloat(credit.quantity || 0), 0) || 0,
        totalValue: project.carbon_credits?.reduce((sum, credit) => sum + parseFloat(credit.total_value || 0), 0) || 0,
        creditBatches: project.carbon_credits?.length || 0,
        monitoringEntries: project.monitoring_data?.length || 0,
        verifiedMonitoring: project.monitoring_data?.filter(m => m.verification_status === 'VERIFIED').length || 0
      }
    }));

    res.json({ 
      data: projectsWithStats,
      count: projectsWithStats.length 
    });
  } catch (error) {
    console.error('Error fetching admin projects:', error);
    res.status(500).json({ error: error.message });
  }
});

// Admin: Get dashboard statistics
router.get('/dashboard-stats', async (req, res) => {
  try {
    // Get overall statistics
    const [projectsCount, creditsCount, monitoringCount, usersCount] = await Promise.all([
      supabaseAdmin.from('projects').select('id', { count: 'exact', head: true }),
      supabaseAdmin.from('carbon_credits').select('id', { count: 'exact', head: true }),
      supabaseAdmin.from('monitoring_data').select('id', { count: 'exact', head: true }),
      supabaseAdmin.from('user_profiles').select('id', { count: 'exact', head: true })
    ]);

    // Get total credits and value
    const { data: creditStats } = await supabaseAdmin
      .from('carbon_credits')
      .select('quantity, total_value');

    const totalCredits = creditStats?.reduce((sum, credit) => sum + parseFloat(credit.quantity || 0), 0) || 0;
    const totalValue = creditStats?.reduce((sum, credit) => sum + parseFloat(credit.total_value || 0), 0) || 0;

    // Get recent activity
    const { data: recentProjects } = await supabaseAdmin
      .from('projects')
      .select('project_name, status, created_at')
      .order('created_at', { ascending: false })
      .limit(5);

    res.json({
      stats: {
        totalProjects: projectsCount.count || 0,
        totalCredits: Math.round(totalCredits),
        totalValue: Math.round(totalValue),
        totalMonitoring: monitoringCount.count || 0,
        totalUsers: usersCount.count || 0
      },
      recentActivity: recentProjects || []
    });
  } catch (error) {
    console.error('Error fetching dashboard stats:', error);
    res.status(500).json({ error: error.message });
  }
});

// Admin: Update project status
router.patch('/projects/:id/status', async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const { data, error } = await supabaseAdmin
      .from('projects')
      .update({ 
        status,
        updated_at: new Date().toISOString()
      })
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;

    res.json({ 
      message: 'Project status updated successfully',
      data 
    });
  } catch (error) {
    console.error('Error updating project status:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
