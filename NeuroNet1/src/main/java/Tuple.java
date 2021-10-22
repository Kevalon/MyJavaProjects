import java.io.FileWriter;
import java.io.IOException;

public class Tuple {
    private final Integer from;
    private final Integer to;
    private final Integer order;

    public Tuple(Integer fr, Integer t, Integer ord) {
        from = fr;
        to = t;
        order = ord;
    }

    public void writeToFile(FileWriter out) throws IOException {
        out.append("        <arc>\n");
        out.append("            <from>v");
        out.append(String.valueOf(from)).append("</from>\n");
        out.append("            <to>v");
        out.append(String.valueOf(to)).append("</to>\n");
        out.append("            <order>");
        out.append(String.valueOf(order)).append("</order>\n");
        out.append("        </arc>\n");
    }
}
