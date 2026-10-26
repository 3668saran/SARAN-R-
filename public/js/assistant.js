let currentSubject = "Computer Science";

function setSubject(subj, btn) {
  currentSubject = subj;
  document.querySelectorAll(".container button").forEach(b => b.classList.remove("active"));
  if (btn) btn.classList.add("active");
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("chat-form");
  const input = document.getElementById("chat-input");
  const messagesBox = document.getElementById("chat-messages");
  const sendBtn = document.getElementById("send-btn");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const prompt = input.value.trim();
    if (!prompt) return;

    // Append user message
    const userDiv = document.createElement("div");
    userDiv.className = "msg-user";
    userDiv.innerText = prompt;
    messagesBox.appendChild(userDiv);
    input.value = "";
    sendBtn.disabled = true;
    sendBtn.innerText = "Thinking...";

    // Append loading placeholder
    const aiDiv = document.createElement("div");
    aiDiv.className = "msg-ai";
    aiDiv.innerText = "Gemma 4 is synthesizing your response...";
    messagesBox.appendChild(aiDiv);
    messagesBox.scrollTop = messagesBox.scrollHeight;

    try {
      const res = await StudyMateApi.askAI(prompt, currentSubject);
      aiDiv.innerHTML = res.response.replace(/\n/g, "<br>").replace(/```([\s\S]*?)```/g, "<pre><code>$1</code></pre>");
    } catch (err) {
      aiDiv.innerText = "Error: Could not retrieve response. Please check your network or API settings.";
    } finally {
      sendBtn.disabled = false;
      sendBtn.innerText = "Ask AI";
      messagesBox.scrollTop = messagesBox.scrollHeight;
    }
  });
});
