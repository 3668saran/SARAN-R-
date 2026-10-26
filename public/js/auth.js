// StudyMate Authentication Controller

class AuthController {
  static init() {
    this.updateUserUI();
  }

  static getCurrentUser() {
    const userJson = localStorage.getItem("studymate_user");
    if (userJson) {
      try { return JSON.parse(userJson); } catch (e) {}
    }
    // Default logged in demo student
    return {
      name: "Alex Morgan",
      email: "alex.morgan@university.edu",
      role: "student",
      status: "verified"
    };
  }

  static switchRole(role) {
    let token = "dev-token-student";
    let user = { name: "Alex Morgan (Student)", role: "student", status: "verified" };
    
    if (role === "staff") {
      token = "dev-token-staff";
      user = { name: "Dr. Evelyn Vance (Staff)", role: "staff", status: "verified" };
    } else if (role === "admin") {
      token = "dev-token-admin";
      user = { name: "Dean Robert Hayes (Admin)", role: "admin", status: "verified" };
    }
    
    localStorage.setItem("studymate_token", token);
    localStorage.setItem("studymate_user", JSON.stringify(user));
    window.location.reload();
  }

  static logout() {
    localStorage.removeItem("studymate_token");
    localStorage.removeItem("studymate_user");
    window.location.href = "/login.html";
  }

  static updateUserUI() {
    const user = this.getCurrentUser();
    const userDisplay = document.getElementById("nav-user-name");
    if (userDisplay && user) {
      userDisplay.innerText = user.name;
    }
  }
}

window.AuthController = AuthController;
document.addEventListener("DOMContentLoaded", () => AuthController.init());
