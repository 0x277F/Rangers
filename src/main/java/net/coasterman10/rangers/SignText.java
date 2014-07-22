package net.coasterman10.rangers;

public class SignText {
    private String[] lines;

    public SignText() {
        lines = new String[] { "", "", "", "" };
    }

    public SignText(String[] lines) {
        this.lines = lines;
    }

    public SignText setLine(int index, String line) {
        lines[index] = line;
        return this;
    }
    
    public boolean matches(String[] lines) {
        for (int i = 0; i < this.lines.length && i < lines.length; i++) {
            if (!this.lines[i].equals("") && !this.lines[i].equals(lines[i])) {
                return false;
            }
        }
        return true;
    }
}
