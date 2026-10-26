# StudyMate AI — Learn, Share, Verify
### Complete Full-Stack Student Learning & Credential Verification Platform

StudyMate AI is a comprehensive student platform integrating:
- **Gemma 4 AI Academic Assistant**: Step-by-step math and STEM solutions, code breakdowns, and practice quiz generation.
- **SkillSwap**: Peer-to-peer 1-on-1 skill exchange between campus students.
- **Student Community Forum**: Academic discussions, notes sharing, and upvoting with staff moderation.
- **Quizzes & Challenges**: AI-curated practice quizzes, instant explanations, and verified leaderboards.
- **Study Planner**: Course task tracking, priority flags, and deadlines.
- **TrustCheck Credential Verification**: Secure marksheet submission, AI document pre-screening, authorized human staff review queue, and tamper-resistant audit logs.

---

## 1. Technology Stack

- **Mobile Client**: Native Android Application with Jetpack Compose & Material 3.
- **Web Frontend**: HTML5, Responsive CSS3 design system, and Vanilla JavaScript.
- **Backend API**: Python 3.10+ Flask REST API with Blueprints.
- **AI Engine**: Google Gemma 4 / Gemini API (`gemini-3.5-flash` or configurable model).
- **Authentication**: Firebase Authentication (Email/Password, Google OAuth, Session Tokens).
- **Database**: Firebase Cloud Firestore with zero-trust security rules.
- **Storage**: Firebase Cloud Storage for private academic marksheets.

---

## 2. Windows Quickstart Setup Guide

### Step 1: Install Python & Clone Repository
1. Ensure Python 3.10 or higher is installed from [python.org](https://www.python.org/) (ensure *"Add Python to PATH"* is checked during installation).
2. Open Windows Command Prompt (`cmd`) or PowerShell in your project folder.

### Step 2: Create Virtual Environment
```cmd
python -m venv venv
venv\Scripts\activate
```

### Step 3: Install Dependencies
```cmd
pip install -r requirements.txt
```

### Step 4: Configure Local Environment (`.env`)
Copy `.env.example` to `.env`:
```cmd
copy .env.example .env
```
Edit `.env` to include your Gemma / Gemini API key and Firebase settings:
```env
GEMMA_API_KEY=your_gemini_api_key_here
GEMMA_MODEL=gemini-3.5-flash
FIREBASE_PROJECT_ID=studymate-ai-demo
FLASK_SECRET_KEY=change_this_to_a_random_secure_key
PORT=5000
```

### Step 5: Start the Flask Backend Server
```cmd
python app.py
```
Open your browser to: **`http://localhost:5000`**

---

## 3. Firebase Configuration & Rules Deployment

### Firebase Setup Steps:
1. Go to [Firebase Console](https://console.firebase.google.com/) and create a project named `studymate-ai`.
2. Navigate to **Authentication** > **Sign-in method** > Enable **Email/Password** and **Google**.
3. Navigate to **Firestore Database** > Create Database in production mode.
4. Navigate to **Storage** > Create Cloud Storage bucket.
5. In Project Settings > Service Accounts > Click **"Generate new private key"** and save as `serviceAccountKey.json` in your project root.

### Deploying Security Rules:
Install Firebase CLI:
```cmd
npm install -g firebase-tools
firebase login
firebase init firestore
firebase init storage
```
Deploy the included zero-trust rules:
```cmd
firebase deploy --only firestore:rules,storage:rules
```

---

## 4. API Endpoints Reference

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/health` | Health & model status | Public |
| `POST` | `/api/ai/chat` | Ask Gemma AI tutor | Student/Staff |
| `POST` | `/api/ai/generate-quiz` | Generate AI practice quiz | Student/Staff |
| `GET` | `/api/community/posts` | List discussion posts | Public |
| `POST` | `/api/community/posts` | Create new discussion | Authenticated |
| `POST` | `/api/community/posts/:id/upvote`| Upvote discussion | Authenticated |
| `GET` | `/api/skills/listings` | Search peer skills | Public |
| `POST` | `/api/skills/requests` | Propose skill exchange | Authenticated |
| `GET` | `/api/quizzes` | List practice quizzes | Public |
| `POST` | `/api/quizzes/:id/submit` | Validate & score quiz | Authenticated |
| `GET` | `/api/quizzes/leaderboard` | View student leaderboard | Public |
| `GET` | `/api/planner/tasks` | List user study tasks | Owner Only |
| `POST` | `/api/planner/tasks` | Add study task | Owner Only |
| `POST` | `/api/trustcheck/submit` | Submit marksheet for review| Student |
| `GET` | `/api/trustcheck/staff/queue`| Review queue of documents | Staff / Admin |
| `POST` | `/api/trustcheck/staff/review`| Approve or reject with reason| Staff / Admin |
| `GET` | `/api/trustcheck/audit` | View immutable audit logs | Staff / Admin |

---

## 5. Security & Academic Integrity Guidelines

- **Zero Plaintext Storage**: Passwords are never stored in databases; Firebase Authentication handles all hash verification.
- **Least-Privilege RBAC**: Student accounts cannot access the staff review queue (`403 Forbidden` enforced on server).
- **Human-in-the-Loop Verification**: Gemma 4 AI provides extraction assistance and advisory flags only. Final marksheet verification requires an authorized human reviewer.
- **Data Privacy**: Student registration numbers and uploaded marksheets are private records stored in private buckets with restricted access.
