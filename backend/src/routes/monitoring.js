const express = require('express');
const router = express.Router();
const { supabase, supabaseAdmin } = require('../config/supabase');

// Get all monitoring data
router.get('/', async (req, res) => {
  try {
    const { project_id } = req.query;

    let query = supabaseAdmin
      .from('monitoring_data')
      .select(`
        *,
        projects:project_id (
          project_name,
          project_type
        )
      `)
      .order('created_at', { ascending: false });

    if (project_id) {
      query = query.eq('project_id', project_id);
    }

    const { data, error } = await query;

    if (error) throw error;

    res.json({ data, count: data?.length || 0 });
  } catch (error) {
    console.error('Error fetching monitoring data:', error);
    res.status(500).json({ error: error.message });
  }
});

// Create monitoring data entry
router.post('/', async (req, res) => {
  try {
    const monitoringData = req.body;

    const { data, error } = await supabase
      .from('monitoring_data')
      .insert([monitoringData])
      .select()
      .single();

    if (error) throw error;

    res.status(201).json({ 
      message: 'Monitoring data created successfully',
      data 
    });
  } catch (error) {
    console.error('Error creating monitoring data:', error);
    res.status(500).json({ error: error.message });
  }
});

// Update verification status
router.patch('/:id/verification', async (req, res) => {
  try {
    const { id } = req.params;
    const { verification_status } = req.body;

    const { data, error } = await supabaseAdmin
      .from('monitoring_data')
      .update({ 
        verification_status,
        updated_at: new Date().toISOString()
      })
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;

    res.json({ 
      message: 'Verification status updated successfully',
      data 
    });
  } catch (error) {
    console.error('Error updating verification status:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
