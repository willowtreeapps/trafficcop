package com.willowtreeapps.trafficcop;

/**
 * Created by evantatarka on 10/7/14.
 */
public class DataUsage {
    public final Type type;
    public final long bytes;
    public final int seconds;

    DataUsage(Type type, long bytes, int seconds) {
        this.type = type;
        this.bytes = bytes;
        this.seconds = seconds;
    }

    public static Builder download(int amount, SizeUnit unit) {
        return new Builder(Type.DOWNLOAD, unit.of(amount));
    }

    public static Builder upload(int amount, SizeUnit unit) {
        return new Builder(Type.UPLOAD, unit.of(amount));
    }

    public static enum Type {
        DOWNLOAD, UPLOAD
    }

    public String getWarningMessage() {
        return "Warning! You have used " + getHumanReadableSize() + " in " + getHumanReadableTimespan() + ".";
    }

    public String getHumanReadableSize() {
        if (bytes < 1000) {
            return bytes + " bytes";
        }
        if (bytes < 1000 * 1000) {
            return (bytes / 1000) + " kilobytes";
        }
        if (bytes < 1000 * 1000 * 1000) {
            return (bytes / (1000 * 1000)) + " megabytes";
        }
        return (bytes / (1000 * 1000 * 1000)) + " gigabytes";
    }

    public String getHumanReadableTimespan() {
        if (seconds < 60) {
            return seconds + " seconds";
        }
        if (seconds < 60 * 60) {
            return (seconds / 60) + " minutes";
        }
        if (seconds < 60 * 60 * 24) {
            return (seconds / (60 * 60)) + " hours";
        }
        return (seconds / (60 * 60 * 24)) + " days";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataUsage usage = (DataUsage) o;

        return bytes == usage.bytes && seconds == usage.seconds && type == usage.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (bytes ^ (bytes >>> 32));
        result = 31 * result + seconds;
        return result;
    }

    @Override
    public String toString() {
        return type + " " + getHumanReadableSize() + " in " + getHumanReadableTimespan();
    }

    public static class Builder {
        private Type type;
        private long bytes;
        private int seconds;

        private Builder(Type type, long bytes) {
            this.type = type;
            this.bytes = bytes;
        }

        public DataUsage in(int amount, TimeUnit unit) {
            seconds = unit.of(amount);
            return new DataUsage(type, bytes, seconds);
        }
    }
}
