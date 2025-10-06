const express = require("express");
const router = express.Router();
const { supabase, supabaseAdmin } = require("../config/supabase");

// Get all carbon credits (for users)
router.get("/", async (req, res) => {
  try {
    const { data, error } = await supabase
      .from("carbon_credits")
      .select(
        `
        *,
        projects!carbon_credits_project_id_fkey (
          project_name,
          project_type,
          country,
          state
        )
      `
      )
      .order("created_at", { ascending: false });

    if (error) throw error;

    res.json({ data, count: data?.length || 0 });
  } catch (error) {
    console.error("Error fetching credits:", error);
    res.status(500).json({ error: error.message });
  }
});

// Create new carbon credit batch
router.post("/", async (req, res) => {
  try {
    const {
      user_id,
      project_id,
      batch_id,
      quantity,
      price_per_tonne,
      methodology,
      standard,
      vintage_year,
    } = req.body;

    const total_value = quantity * (price_per_tonne || 60); // Default $60/tonne

    const { data, error } = await supabase
      .from("carbon_credits")
      .insert([
        {
          user_id,
          project_id,
          batch_id,
          quantity,
          price_per_tonne: price_per_tonne || 60,
          total_value,
          status: "ISSUED",
          issue_date: new Date().toISOString().split("T")[0],
          vintage_year: vintage_year || new Date().getFullYear(),
          methodology: methodology || "VCS VM0007",
          standard: standard || "VCS + Hedera",
          registry: "BlueRoots Hedera Registry",
          verification_body: "Hedera Guardian Network",
        },
      ])
      .select()
      .single();

    if (error) throw error;

    res.status(201).json({
      success: true,
      message: "Carbon credit batch created successfully",
      data,
    });
  } catch (error) {
    console.error("Error creating credit:", error);
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// Get credit by batch ID
router.get("/batch/:batchId", async (req, res) => {
  try {
    const { batchId } = req.params;

    const { data, error } = await supabase
      .from("carbon_credits")
      .select(
        `
        *,
        projects!carbon_credits_project_id_fkey (
          project_name,
          project_type,
          country,
          state,
          project_area
        )
      `
      )
      .eq("batch_id", batchId)
      .single();

    if (error) throw error;

    res.json({ data });
  } catch (error) {
    console.error("Error fetching credit batch:", error);
    res.status(500).json({ error: error.message });
  }
});

// Update credit status
router.patch("/:id/status", async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const { data, error } = await supabase
      .from("carbon_credits")
      .update({
        status,
        updated_at: new Date().toISOString(),
      })
      .eq("id", id)
      .select()
      .single();

    if (error) throw error;

    res.json({
      message: "Credit status updated successfully",
      data,
    });
  } catch (error) {
    console.error("Error updating credit status:", error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
