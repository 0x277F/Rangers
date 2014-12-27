package net.coasterman10.rangers.util;

public class SignText {
    private String[] lines;

    public SignText() {
        lines = new String[] { "", "", "", "" };
    }

    public SignText(String[] lines) {
        this();
        for (int i = 0; i < 4 && i < lines.length; i++) {
            this.lines[i] = sanitizeLine(lines[i]);
        }
    }

    public SignText setLine(int index, String line) {
        lines[index] = sanitizeLine(line);
        return this;
    }
    
    public String getLine(int line) {
        return lines[line];
    }
    
    private static String sanitizeLine(String line) {
        if (line == null) {
            line = "";
        } else if (line.length() > 15) {
            line = line.substring(0, 15);
        }
        return line;
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        for (String line : lines)
            result = 31 * result + line.toLowerCase().hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof SignText && other.hashCode() == hashCode();
    }
}
