document.addEventListener("DOMContentLoaded", async () => {
  try {
    // Load Tasks
    const tasksRes = await StudyMateApi.getTasks();
    const tasksContainer = document.getElementById("dashboard-tasks-list");
    if (tasksContainer && tasksRes.tasks) {
      if (tasksRes.tasks.length === 0) {
        tasksContainer.innerHTML = "<p style='color: var(--text-muted); font-size: 13px;'>No pending tasks.</p>";
      } else {
        tasksContainer.innerHTML = tasksRes.tasks.slice(0, 3).map(t => `
          <div style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid var(--border);">
            <div>
              <div style="font-weight: 600; font-size: 13px;">${t.title}</div>
              <div style="font-size: 11px; color: var(--text-muted);">${t.subject} • Due ${t.dueDate}</div>
            </div>
            <span class="badge ${t.isCompleted ? 'badge-verified' : 'badge-pending'}">${t.isCompleted ? 'Done' : t.priority}</span>
          </div>
        `).join("");
      }
    }

    // Load Skills
    const skillsRes = await StudyMateApi.getSkillListings();
    const skillsContainer = document.getElementById("dashboard-skills-list");
    if (skillsContainer && skillsRes.listings) {
      skillsContainer.innerHTML = skillsRes.listings.slice(0, 2).map(s => `
        <div style="padding: 8px 0; border-bottom: 1px solid var(--border);">
          <div style="font-weight: 600; font-size: 13px;">${s.userName}</div>
          <div style="font-size: 11px; color: var(--primary);">Teaches: ${s.teachSkills.join(", ")}</div>
          <div style="font-size: 11px; color: var(--text-muted);">Wants: ${s.learnSkills.join(", ")}</div>
        </div>
      `).join("");
    }
  } catch (e) {
    console.error("Dashboard error:", e);
  }
});
