package universitysystem;

public class Student {

    private String studentId;
    private String name;
    private String programme;
    private double marks;

    public Student(String studentId, String name, String programme, double marks) {
        this.studentId = studentId;
        this.name = name;
        this.programme = programme;
        this.marks = marks;
    }

    // Getters
    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getProgramme() {
        return programme;
    }

    public double getMarks() {
        return marks;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    // Grade calculation
    public String getGrade() {
        if (marks >= 85) return "A+";
        if (marks >= 75) return "A";
        if (marks >= 65) return "B";
        if (marks >= 55) return "C";
        if (marks >= 45) return "D";
        return "F";
    }

    // GPA calculation (4.0 Scale)
    public double getGpa() {
        if (marks >= 85) return 4.00;
        if (marks >= 75) return 3.75;
        if (marks >= 65) return 3.25;
        if (marks >= 55) return 2.75;
        if (marks >= 45) return 2.00;
        return 0.00;
    }

    // Email generator
    public String getEmail() {
        String cleaned = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (cleaned.isEmpty()) cleaned = "student";
        return cleaned + "." + studentId.toLowerCase() + "@university.edu";
    }

    // JSON representation for web API
    public String toJson() {
        return String.format(
            "{\"studentId\":\"%s\",\"name\":\"%s\",\"programme\":\"%s\",\"marks\":%.2f,\"grade\":\"%s\",\"gpa\":%.2f,\"email\":\"%s\"}",
            escapeJson(studentId),
            escapeJson(name),
            escapeJson(programme),
            marks,
            getGrade(),
            getGpa(),
            escapeJson(getEmail())
        );
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    // Display student information
    @Override
    public String toString() {
        return String.format("ID: %-8s | Name: %-20s | Prog: %-15s | Marks: %-5.1f | Grade: %-2s | GPA: %.2f",
                studentId, name, programme, marks, getGrade(), getGpa());
    }
}