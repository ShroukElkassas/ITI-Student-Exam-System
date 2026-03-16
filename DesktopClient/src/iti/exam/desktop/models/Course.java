package iti.exam.desktop.models;

public final class Course {
    private final int courseId;
    private final String courseName;
    private final Integer minDegree;
    private final Integer maxDegree;
    private final Integer trackId;
    private final String trackName;

    public Course(int courseId, String courseName, Integer minDegree, Integer maxDegree, Integer trackId, String trackName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.minDegree = minDegree;
        this.maxDegree = maxDegree;
        this.trackId = trackId;
        this.trackName = trackName;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public Integer getMinDegree() {
        return minDegree;
    }

    public Integer getMaxDegree() {
        return maxDegree;
    }

    public Integer getTrackId() {
        return trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", minDegree=" + minDegree +
                ", maxDegree=" + maxDegree +
                ", trackId=" + trackId +
                ", trackName='" + trackName + '\'' +
                '}';
    }
}

