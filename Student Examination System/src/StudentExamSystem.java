import java.sql.*;
import java.util.Scanner;

public class StudentExamSystem {

    static final String DB_URL = "jdbc:mysql://localhost:3306/school1";
    static final String USER = "root";
    static final String PASS = "Krunal@1805";

    static Connection conn;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            mainMenu();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void mainMenu() throws SQLException {
        while (true) {
            System.out.println("\nMain Menu:\n1. Admin Login\n2. Student Login\n3. Exit");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    adminLogin();
                    break;
                case 2:
                    studentLogin();
                    break;
                case 3:
                    System.exit(0);
            }
        }
    }

    public static void adminLogin() throws SQLException {
        sc.nextLine(); // consume newline
        System.out.println("Enter Admin Name:");
        String name = sc.nextLine();
        System.out.println("Enter Admin Password:");
        String password = sc.nextLine();
    
        // Query to check if the admin credentials are valid
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM admin WHERE admin_name = ? AND admin_password = ?");
        ps.setString(1, name);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
    
        if (rs.next()) {
            System.out.println("Login successful.");
            adminMenu();
        } else {
            System.out.println("Invalid credentials.");
        }
    }
    

    public static void adminMenu() throws SQLException {
        while (true) {
            System.out.println("\nAdmin Menu:\n1. Create Exam\n2. Add Questions\n3. View Exam Questions\n4. Logout");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    createExam();
                    break;
                case 2:
                    addQuestions();
                    break;
                case 3:
                    viewExamQuestions();
                    break;
                case 4:
                    return;
            }
        }
    }

    public static void createExam() throws SQLException {
        sc.nextLine(); // consume newline
        System.out.println("Enter exam name:");
        String examName = sc.nextLine();
        PreparedStatement ps = conn.prepareStatement("INSERT INTO exam (exam_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, examName);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int examId = rs.getInt(1);
            System.out.println("Exam created successfully with ID: " + examId);
        }
    }

    public static void addQuestions() throws SQLException {
        System.out.println("Available Exams:");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT exam_id, exam_name FROM exam");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("exam_id") + ", Name: " + rs.getString("exam_name"));
        }

        System.out.println("Enter Exam ID to add questions:");
        int examId = sc.nextInt();
        System.out.println("Enter number of questions to add:");
        int questionCount = sc.nextInt();
        sc.nextLine(); // consume newline

        PreparedStatement ps = conn.prepareStatement("INSERT INTO question (exam_id, question_text, correct_answer) VALUES (?, ?, ?)");
        for (int i = 1; i <= questionCount; i++) {
            System.out.println("Enter Question " + i + ":");
            String questionText = sc.nextLine();
            System.out.println("Enter Correct Answer:");
            String correctAnswer = sc.nextLine();
            ps.setInt(1, examId);
            ps.setString(2, questionText);
            ps.setString(3, correctAnswer);
            ps.executeUpdate();
        }
        System.out.println("Questions added successfully.");
    }

    public static void viewExamQuestions() throws SQLException {
        System.out.println("Enter Exam ID:");
        int examId = sc.nextInt();
        PreparedStatement ps = conn.prepareStatement("SELECT question_text, correct_answer FROM question WHERE exam_id = ?");
        ps.setInt(1, examId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println("Question: " + rs.getString("question_text"));
            System.out.println("Answer: " + rs.getString("correct_answer"));
        }
    }

    public static void studentLogin() throws SQLException {
        sc.nextLine(); // consume newline
        System.out.println("Enter Name:");
        String name = sc.nextLine();
        System.out.println("Enter Class:");
        String studentClass = sc.nextLine();
        System.out.println("Enter Enrollment No:");
        String enrollmentNo = sc.nextLine();
    
        // Check if the student already exists
        PreparedStatement ps = conn.prepareStatement("SELECT student_id FROM student WHERE name = ? AND class = ? AND enrollment_no = ?");
        ps.setString(1, name);
        ps.setString(2, studentClass);
        ps.setString(3, enrollmentNo);
        ResultSet rs = ps.executeQuery();
    
        if (rs.next()) {
            int studentId = rs.getInt("student_id");
            System.out.println("Welcome back, " + name + "!");
            studentMenu(studentId);
        } else {
            // If the student is new, register the student
            ps = conn.prepareStatement("INSERT INTO student (name, class, enrollment_no) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, studentClass);
            ps.setString(3, enrollmentNo);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int studentId = rs.getInt(1);
                System.out.println("Registration successful! Welcome, " + name);
                studentMenu(studentId);
            }
        }
    }
    

    public static void studentMenu(int studentId) throws SQLException {
        while (true) {
            System.out.println("\nStudent Menu:\n1. Give Exam\n2. View Score\n3. Logout");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    giveExam(studentId);
                    break;
                case 2:
                    viewScore(studentId);
                    break;
                case 3:
                    return;
            }
        }
    }

    public static void giveExam(int studentId) throws SQLException {
        System.out.println("Enter Exam ID:");
        int examId = sc.nextInt();
        sc.nextLine(); // consume newline
    
        // Check if the student has already taken the exam
        PreparedStatement ps = conn.prepareStatement("SELECT score FROM student_exam WHERE student_id = ? AND exam_id = ?");
        ps.setInt(1, studentId);
        ps.setInt(2, examId);
        ResultSet rs = ps.executeQuery();
    
        if (rs.next()) {
            // If the student has already taken the exam
            System.out.println("You have already given this exam. You can only view your score.");
        } else {
            // If the student has not taken the exam
            System.out.println("Ready to give exam? (Y/N):");
            String confirm = sc.nextLine();
            if (confirm.equalsIgnoreCase("Y")) {
                ps = conn.prepareStatement("SELECT question_id, question_text, correct_answer FROM question WHERE exam_id = ?");
                ps.setInt(1, examId);
                rs = ps.executeQuery();
    
                int score = 0;
                while (rs.next()) {
                    System.out.println(rs.getString("question_text"));
                    String studentAnswer = sc.nextLine();
                    if (studentAnswer.equalsIgnoreCase(rs.getString("correct_answer"))) {
                        score++;
                    }
                }
    
                // Save student score
                ps = conn.prepareStatement("INSERT INTO student_exam (student_id, exam_id, score) VALUES (?, ?, ?)");
                ps.setInt(1, studentId);
                ps.setInt(2, examId);
                ps.setInt(3, score);
                ps.executeUpdate();
    
                System.out.println("Your total score: " + score);
            }
        }
    }
    

    public static void viewScore(int studentId) throws SQLException {
        // Retrieve all scores for the student
        PreparedStatement ps = conn.prepareStatement("SELECT exam.exam_name, student_exam.score FROM student_exam JOIN exam ON student_exam.exam_id = exam.exam_id WHERE student_exam.student_id = ?");
        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();
    
        System.out.println("Your exam scores:");
        boolean hasScores = false;
        while (rs.next()) {
            System.out.println("Exam: " + rs.getString("exam_name") + " | Score: " + rs.getInt("score"));
            hasScores = true;
        }
    
        if (!hasScores) {
            System.out.println("You haven't taken any exams yet.");
        }
    }
    
}
