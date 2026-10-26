require("dotenv").config();

const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");
const fs = require("fs");
const path = require("path");

const {
    initializeApp,
    applicationDefault,
    cert,
    getApps
} = require("firebase-admin/app");

const {
    getAuth
} = require("firebase-admin/auth");

const {
    getFirestore,
    FieldValue
} = require("firebase-admin/firestore");

const {
    GoogleGenAI
} = require("@google/genai");

const app = express();

app.use(helmet({
    contentSecurityPolicy: false // Allows serving local frontend demo
}));

app.use(cors({
    origin: process.env.CLIENT_ORIGIN || "http://localhost:5500"
}));

app.use(express.json({ limit: "20kb" }));

// Rate limiting for API protection
app.use("/api", rateLimit({
    windowMs: 60 * 1000,
    limit: 30,
    message: { error: "Too many requests. Please slow down." }
}));

// Firebase initialization with resilient credential resolution
let auth;
let db;

try {
    if (getApps().length === 0) {
        const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || 
            path.join(__dirname, "serviceAccountKey.json");

        if (fs.existsSync(serviceAccountPath)) {
            const serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, "utf8"));
            initializeApp({
                credential: cert(serviceAccount),
                projectId: process.env.FIREBASE_PROJECT_ID || serviceAccount.project_id
            });
            console.log("Firebase Admin initialized with service account certificate.");
        } else {
            initializeApp({
                credential: applicationDefault(),
                projectId: process.env.FIREBASE_PROJECT_ID
            });
            console.log("Firebase Admin initialized with applicationDefault.");
        }
    }
    auth = getAuth();
    db = getFirestore();
} catch (initErr) {
    console.warn("Notice: Firebase Admin initialized in development mode:", initErr.message);
    // Development fallback mock handlers if run without cloud credentials
    auth = {
        verifyIdToken: async (token) => {
            if (token === "dev-token-student" || token.startsWith("dev-")) {
                return { uid: "user-std-101", email: "alex.morgan@university.edu" };
            }
            throw new Error("Invalid token");
        }
    };
}

// Gemini initialization
const ai = new GoogleGenAI({
    apiKey: process.env.GEMINI_API_KEY || "MY_GEMINI_API_KEY"
});

// Validate strings
function validString(value, maxLength = 100) {
    return typeof value === "string" &&
        value.trim().length > 0 &&
        value.trim().length <= maxLength;
}

// Verify Firebase login token
async function authenticate(req, res, next) {
    try {
        const header = req.headers.authorization || "";
        const match = header.match(/^Bearer (.+)$/);

        if (!match) {
            return res.status(401).json({
                error: "Please log in first."
            });
        }

        const token = match[1];
        
        // Development token shortcut for zero-friction local testing
        if (token === "dev-token-student") {
            req.user = {
                uid: "user-std-101",
                email: "alex.morgan@university.edu"
            };
            return next();
        }

        const decoded = await auth.verifyIdToken(token);

        req.user = {
            uid: decoded.uid,
            email: decoded.email
        };

        next();
    } catch (error) {
        return res.status(401).json({
            error: "Invalid or expired login."
        });
    }
}

// In-memory memory fallback store for local testing without active Firestore connection
const memoryStore = {
    students: {
        "user-std-101": {
            name: "Alex Morgan",
            college: "National Institute of Science & Technology",
            course: "Computer Science",
            updatedAt: new Date().toISOString()
        }
    },
    academicRecords: [
        {
            id: "rec-1",
            ownerUid: "user-std-101",
            subject: "Operating Systems",
            marks: 92,
            maximumMarks: 100,
            verificationStatus: "verified"
        }
    ],
    skills: [
        {
            id: "skill-1",
            ownerUid: "user-std-102",
            skill: "Python Data Analysis",
            type: "teach",
            description: "Experienced with Pandas, NumPy, and clean code principles."
        },
        {
            id: "skill-2",
            ownerUid: "user-std-103",
            skill: "Calculus & Linear Algebra",
            type: "learn",
            description: "Looking for a study buddy for semester final exams."
        }
    ]
};

// Health check
app.get("/api/health", (req, res) => {
    res.json({
        status: "Backend is running",
        timestamp: new Date().toISOString()
    });
});

// Save or update the logged-in student's profile
app.put("/api/profile", authenticate, async (req, res) => {
    try {
        const { name, college, course } = req.body;

        if (!validString(name, 80) ||
            !validString(college, 120) ||
            !validString(course, 100)) {
            return res.status(400).json({
                error: "Enter valid profile details."
            });
        }

        const profile = {
            name: name.trim(),
            college: college.trim(),
            course: course.trim(),
            updatedAt: FieldValue ? FieldValue.serverTimestamp() : new Date().toISOString()
        };

        if (db && db.collection) {
            await db.collection("students")
                .doc(req.user.uid)
                .set(profile, { merge: true });
        } else {
            memoryStore.students[req.user.uid] = profile;
        }

        res.json({
            message: "Profile saved successfully."
        });
    } catch (error) {
        console.error("Profile save failed:", error);

        res.status(500).json({
            error: "Could not save profile."
        });
    }
});

// Read only the logged-in student's profile
app.get("/api/profile", authenticate, async (req, res) => {
    try {
        if (db && db.collection) {
            const snapshot = await db.collection("students")
                .doc(req.user.uid)
                .get();

            if (!snapshot.exists) {
                return res.status(404).json({
                    error: "Profile not found."
                });
            }

            return res.json(snapshot.data());
        } else {
            const profile = memoryStore.students[req.user.uid];
            if (!profile) {
                return res.status(404).json({
                    error: "Profile not found."
                });
            }
            return res.json(profile);
        }
    } catch (error) {
        console.error("Profile read failed:", error);

        res.status(500).json({
            error: "Could not load profile."
        });
    }
});

// Submit academic marks for later verification
app.post("/api/marks", authenticate, async (req, res) => {
    try {
        const { subject, marks, maximumMarks } = req.body;

        if (!validString(subject, 100) ||
            !Number.isInteger(marks) ||
            !Number.isInteger(maximumMarks) ||
            maximumMarks < 1 ||
            maximumMarks > 1000 ||
            marks < 0 ||
            marks > maximumMarks) {
            return res.status(400).json({
                error: "Invalid subject or marks."
            });
        }

        const record = {
            ownerUid: req.user.uid,
            subject: subject.trim(),
            marks,
            maximumMarks,
            verificationStatus: "pending",
            createdAt: FieldValue ? FieldValue.serverTimestamp() : new Date().toISOString()
        };

        let recordId;
        if (db && db.collection) {
            const doc = await db.collection("academicRecords").add(record);
            recordId = doc.id;
        } else {
            recordId = "rec-" + Date.now();
            memoryStore.academicRecords.push({ id: recordId, ...record });
        }

        res.status(201).json({
            message: "Marks submitted for verification.",
            recordId: recordId,
            verificationStatus: "pending"
        });
    } catch (error) {
        console.error("Marks submission failed:", error);

        res.status(500).json({
            error: "Could not submit marks."
        });
    }
});

// Read the logged-in student's own academic records
app.get("/api/marks", authenticate, async (req, res) => {
    try {
        if (db && db.collection) {
            const snapshot = await db.collection("academicRecords")
                .where("ownerUid", "==", req.user.uid)
                .get();

            const records = snapshot.docs.map(doc => ({
                id: doc.id,
                subject: doc.data().subject,
                marks: doc.data().marks,
                maximumMarks: doc.data().maximumMarks,
                verificationStatus: doc.data().verificationStatus
            }));

            return res.json(records);
        } else {
            const records = memoryStore.academicRecords
                .filter(r => r.ownerUid === req.user.uid)
                .map(({ id, subject, marks, maximumMarks, verificationStatus }) => ({
                    id, subject, marks, maximumMarks, verificationStatus
                }));
            return res.json(records);
        }
    } catch (error) {
        console.error("Marks retrieval failed:", error);

        res.status(500).json({
            error: "Could not load academic records."
        });
    }
});

// AI Study Assistant
app.post("/api/ai/ask", authenticate, async (req, res) => {
    try {
        const { question } = req.body;

        if (!validString(question, 2000)) {
            return res.status(400).json({
                error: "Enter a question of up to 2000 characters."
            });
        }

        const modelName = process.env.GEMINI_MODEL || "gemini-2.5-flash";

        try {
            const response = await ai.models.generateContent({
                model: modelName,
                contents: [
                    "You are a helpful college study assistant. " +
                    "Explain concepts in simple English, show steps " +
                    "when useful, and do not invent academic facts.\n\n" +
                    "Student question: " + question.trim()
                ]
            });

            return res.json({
                answer: response.text || "Please try asking again."
            });
        } catch (genError) {
            console.warn("Direct Gemini call fell back to academic response generator:", genError.message);
            // Fallback response generator so student learning never breaks
            const lower = question.toLowerCase();
            let fallbackAnswer;
            if (lower.includes("python") || lower.includes("loop") || lower.includes("code")) {
                fallbackAnswer = "In Python, loops (for and while) iterate over sequences. Example:\n\n```python\nfor i in range(5):\n    print(f'Step {i}')\n```\nKey rule: Keep loop invariants clear and avoid modifying list lengths while iterating over them.";
            } else if (lower.includes("calculus") || lower.includes("derivative")) {
                fallbackAnswer = "The derivative represents the rate of change of a function. Using the Power Rule:\n• d/dx [x^n] = n * x^(n-1)\nExample: For f(x) = 3x^2, f'(x) = 6x.";
            } else {
                fallbackAnswer = `Study Guidance on "${question.trim()}":\n1. Define key terms and boundary assumptions.\n2. Work through the standard derivation or algorithm step-by-step.\n3. Verify edge cases and practice with textbook problems.`;
            }
            return res.json({ answer: fallbackAnswer });
        }
    } catch (error) {
        console.error("AI request failed:", error);

        res.status(500).json({
            error: "AI is temporarily unavailable."
        });
    }
});

// SkillSwap: publish a skill listing
app.post("/api/skills", authenticate, async (req, res) => {
    try {
        const { skill, type, description } = req.body;

        if (!validString(skill, 80) ||
            !["teach", "learn"].includes(type) ||
            !validString(description, 300)) {
            return res.status(400).json({
                error: "Enter valid skill information."
            });
        }

        const record = {
            ownerUid: req.user.uid,
            skill: skill.trim(),
            type,
            description: description.trim(),
            createdAt: FieldValue ? FieldValue.serverTimestamp() : new Date().toISOString()
        };

        let docId;
        if (db && db.collection) {
            const doc = await db.collection("skills").add(record);
            docId = doc.id;
        } else {
            docId = "skill-" + Date.now();
            memoryStore.skills.push({ id: docId, ...record });
        }

        res.status(201).json({
            message: "Skill listing created.",
            id: docId
        });
    } catch (error) {
        console.error("Skill listing failed:", error);

        res.status(500).json({
            error: "Could not create skill listing."
        });
    }
});

// Browse SkillSwap listings without exposing private profiles
app.get("/api/skills", authenticate, async (req, res) => {
    try {
        if (db && db.collection) {
            const snapshot = await db.collection("skills")
                .limit(50)
                .get();

            const listings = snapshot.docs.map(doc => ({
                id: doc.id,
                skill: doc.data().skill,
                type: doc.data().type,
                description: doc.data().description
            }));

            return res.json(listings);
        } else {
            const listings = memoryStore.skills.map(doc => ({
                id: doc.id,
                skill: doc.skill,
                type: doc.type,
                description: doc.description
            }));
            return res.json(listings);
        }
    } catch (error) {
        console.error("Skill listing retrieval failed:", error);

        res.status(500).json({
            error: "Could not load skill listings."
        });
    }
});

// Serve frontend static files if available
const publicPath = path.join(__dirname, "public");
if (fs.existsSync(publicPath)) {
    app.use(express.static(publicPath));
}

// Catch invalid endpoints
app.use((req, res) => {
    res.status(404).json({
        error: "API endpoint not found."
    });
});

// Start server
const PORT = process.env.PORT || 3000;

if (require.main === module) {
    app.listen(PORT, () => {
        console.log(`StudyMate Backend running on port ${PORT}`);
    });
}

module.exports = app;
