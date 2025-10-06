const express = require('express');
const router = express.Router();
const { supabase, supabaseAdmin } = require('../config/supabase');

// Get all projects
router.get('/', async (req, res) => {
  try {
    const { data, error } = await supabaseAdmin
      .from('projects')
      .select(`
        *,
        user_profiles(
          full_name,
          organization_name,
          email
        )
      `)
      .order('created_at', { ascending: false });

    if (error) throw error;

    res.json({ data, count: data?.length || 0 });
  } catch (error) {
    console.error('Error fetching projects:', error);
    res.status(500).json({ error: error.message });
  }
});

// Get project by ID
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const { data, error } = await supabaseAdmin
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
          batch_id,
          quantity,
          status,
          total_value
        )
      `)
      .eq('id', id)
      .single();

    if (error) throw error;

    res.json({ data });
  } catch (error) {
    console.error('Error fetching project:', error);
    res.status(500).json({ error: error.message });
  }
});

// Create new project
router.post('/', async (req, res) => {
  try {
    const {
      user_id,
      project_name,
      project_description,
      project_type,
      latitude,
      longitude,
      country,
      state,
      district,
      nearest_city,
      project_area,
      start_date,
      estimated_investment,
      funding_source,
      expected_credit_generation,
      methodology,
      project_developer
    } = req.body;

    const projectData = {
      user_id,
      project_name,
      project_description,
      project_type: project_type || 'MANGROVE',
      status: 'PLANNING',
      latitude: parseFloat(latitude) || 0,
      longitude: parseFloat(longitude) || 0,
      country,
      state,
      district,
      nearest_city,
      project_area: parseFloat(project_area) || 0,
      start_date: start_date || new Date().toISOString(),
      estimated_investment: parseFloat(estimated_investment) || 0,
      funding_source,
      expected_credit_generation: parseFloat(expected_credit_generation) || 0,
      methodology,
      project_developer: JSON.stringify(project_developer),
      created_at: new Date().toISOString()
    };

    const { data, error } = await supabase
      .from('projects')
      .insert([projectData])
      .select()
      .single();

    if (error) throw error;

    res.status(201).json({ 
      success: true,
      message: 'Project registered successfully',
      data 
    });
  } catch (error) {
    console.error('Error creating project:', error);
    res.status(500).json({ 
      success: false,
      error: error.message 
    });
  }
});

// Update project
router.patch('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const updates = req.body;

    const { data, error } = await supabase
      .from('projects')
      .update({
        ...updates,
        updated_at: new Date().toISOString()
      })
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;

    res.json({ 
      message: 'Project updated successfully',
      data 
    });
  } catch (error) {
    console.error('Error updating project:', error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
