package iti.exam.desktop.models;

public final class Track {
    private final int trackId;
    private final String trackName;
    private final Integer durationMonths;
    private final int branchId;
    private final String branchName;

    public Track(int trackId, String trackName, Integer durationMonths, int branchId, String branchName) {
        this.trackId = trackId;
        this.trackName = trackName;
        this.durationMonths = durationMonths;
        this.branchId = branchId;
        this.branchName = branchName;
    }

    public int getTrackId() {
        return trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    @Override
    public String toString() {
        return "Track{" +
                "trackId=" + trackId +
                ", trackName='" + trackName + '\'' +
                ", durationMonths=" + durationMonths +
                ", branchId=" + branchId +
                ", branchName='" + branchName + '\'' +
                '}';
    }
}

