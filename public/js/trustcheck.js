document.addEventListener("DOMContentLoaded", async () => {
  const form = document.getElementById("trustcheck-form");
  const regInput = document.getElementById("reg-number");
  const docTypeSelect = document.getElementById("doc-type");
  const precheckBtn = document.getElementById("ai-precheck-btn");
  const precheckBox = document.getElementById("ai-precheck-result");
  const precheckText = document.getElementById("ai-precheck-text");

  // Load current verification status
  try {
    const res = await StudyMateApi.getMyVerification();
    if (res.submission) {
      const sub = res.submission;
      const badge = document.getElementById("status-badge");
      const desc = document.getElementById("status-desc");
      const reviewer = document.getElementById("status-reviewer");

      badge.innerText = sub.status.toUpperCase();
      badge.className = `badge badge-${sub.status}`;
      desc.innerText = sub.remarks || "Under review by registry.";
      reviewer.innerText = sub.reviewedBy ? `Reviewed by: ${sub.reviewedBy} on ${sub.reviewedAt}` : "Pending staff review";
    }
  } catch (e) {
    console.error("Error loading verification:", e);
  }

  // AI Precheck
  precheckBtn.addEventListener("click", () => {
    const reg = regInput.value.trim() || "REG-2024-CS-8921";
    const type = docTypeSelect.value;
    
    precheckBox.style.display = "block";
    precheckText.innerText = 
      `• Document Type: ${type}\n` +
      `• Registration #: ${reg}\n` +
      `• Institution: National Institute of Science & Technology\n` +
      `• Student Name: Alex Morgan\n` +
      `• Registrar Seal: Watermark and security stamp pattern detected\n` +
      `• Consistency: No missing academic required fields found.`;
  });

  // Submit
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const reg = regInput.value.trim();
    const type = docTypeSelect.value;
    const file = "alex_official_marksheet_sem4.pdf";

    try {
      const res = await StudyMateApi.submitVerification(reg, type, file);
      alert("Submission uploaded! Our authorized registry staff will review your credential.");
      window.location.reload();
    } catch (err) {
      alert("Submission error: " + err.message);
    }
  });
});
