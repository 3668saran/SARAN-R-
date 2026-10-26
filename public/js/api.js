// StudyMate Centralized API Module

const API_BASE = window.location.origin;

class StudyMateApi {
  static getToken() {
    return localStorage.getItem("studymate_token") || "dev-token-student";
  }

  static setToken(token) {
    localStorage.setItem("studymate_token", token);
  }

  static async request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const headers = {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${this.getToken()}`,
      ...(options.headers || {})
    };

    try {
      const response = await fetch(url, { ...options, headers });
      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.error || `HTTP error ${response.status}`);
      }
      return data;
    } catch (err) {
      console.error(`API Error on ${endpoint}:`, err);
      throw err;
    }
  }

  // AI Endpoints
  static async askAI(prompt, subject) {
    return this.request("/api/ai/chat", {
      method: "POST",
      body: JSON.stringify({ prompt, subject })
    });
  }

  static async generateQuiz(topic) {
    return this.request("/api/ai/generate-quiz", {
      method: "POST",
      body: JSON.stringify({ topic })
    });
  }

  // Community Endpoints
  static async getPosts(subject = "All", search = "") {
    return this.request(`/api/community/posts?subject=${encodeURIComponent(subject)}&search=${encodeURIComponent(search)}`);
  }

  static async createPost(title, content, subject) {
    return this.request("/api/community/posts", {
      method: "POST",
      body: JSON.stringify({ title, content, subject })
    });
  }

  static async upvotePost(postId) {
    return this.request(`/api/community/posts/${postId}/upvote`, { method: "POST" });
  }

  // Skills
  static async getSkillListings(query = "") {
    return this.request(`/api/skills/listings?q=${encodeURIComponent(query)}`);
  }

  static async sendSkillRequest(toUserName, teachOffered, learnRequested, message) {
    return this.request("/api/skills/requests", {
      method: "POST",
      body: JSON.stringify({ toUserName, teachOffered, learnRequested, message })
    });
  }

  static async getMySkillRequests() {
    return this.request("/api/skills/my-requests");
  }

  // Quizzes
  static async getQuizzes() {
    return this.request("/api/quizzes");
  }

  static async submitQuiz(quizId, answers) {
    return this.request(`/api/quizzes/${quizId}/submit`, {
      method: "POST",
      body: JSON.stringify({ answers })
    });
  }

  static async getLeaderboard() {
    return this.request("/api/quizzes/leaderboard");
  }

  // Planner
  static async getTasks() {
    return this.request("/api/planner/tasks");
  }

  static async addTask(title, subject, priority, dueDate) {
    return this.request("/api/planner/tasks", {
      method: "POST",
      body: JSON.stringify({ title, subject, priority, dueDate })
    });
  }

  static async toggleTask(taskId) {
    return this.request(`/api/planner/tasks/${taskId}/toggle`, { method: "PUT" });
  }

  // TrustCheck
  static async submitVerification(collegeRegNumber, documentType, documentName) {
    return this.request("/api/trustcheck/submit", {
      method: "POST",
      body: JSON.stringify({ collegeRegNumber, documentType, documentName })
    });
  }

  static async getMyVerification() {
    return this.request("/api/trustcheck/my-submission");
  }

  static async getStaffQueue() {
    return this.request("/api/trustcheck/staff/queue");
  }

  static async reviewSubmission(submissionId, approved, remarks) {
    return this.request("/api/trustcheck/staff/review", {
      method: "POST",
      body: JSON.stringify({ submissionId, approved, remarks })
    });
  }
}

window.StudyMateApi = StudyMateApi;
