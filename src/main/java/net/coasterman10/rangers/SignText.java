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

    @Override
    public int hashCode() {
        return lines.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SignText))
            return false;
        return lines.equals(((SignText) o).lines);
    }
}
