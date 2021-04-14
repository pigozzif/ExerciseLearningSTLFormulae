/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing.chart;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.FontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A  simple  demonstration  showing  how  to  write  a  chart  to  PDF  format  using
 * JFreeChart  and  iText.
 * <p>
 * You  can  download  iText  from  http://www.lowagie.com/iText.
 */

/**
 *
 * @author Luca
 */
public class PDFChartExport {


    /**
     *  Saves  a  chart  to  a  PDF  file.
     *
     *  @param  filename    the  file.
     *  @param  chart    the  chart.
     *  @param  width    the  chart  width.
     *  @param  height    the  chart  height.
     */
    public static void saveChartAsPDF(String filename,
                                      JFreeChart chart,
                                      int width,
                                      int height) throws IOException {
        FontMapper mapper = new DefaultFontMapper();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
        writeChartAsPDF(out, chart, width, height, mapper);
        out.close();
    }

    /**
     *  Writes  a  chart  to  an  output  stream  in  PDF  format.
     *
     *  @param  out    the  output  stream.
     *  @param  chart    the  chart.
     *  @param  width    the  chart  width.
     *  @param  height    the  chart  height.
     *
     */
    private static void writeChartAsPDF(OutputStream out,
                                        JFreeChart chart,
                                        int width,
                                        int height,
                                        FontMapper mapper) throws IOException {

        Rectangle pagesize = new Rectangle(width, height);
        Document document = new Document(pagesize, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.addAuthor("JFreeChart");
            document.addSubject("Demonstration");
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2 = tp.createGraphics(width, height, mapper);
            Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2, r2D);
            g2.dispose();
            cb.addTemplate(tp, 0, 0);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        }
        document.close();
    }


}
