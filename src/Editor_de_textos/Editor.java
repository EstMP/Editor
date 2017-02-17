/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Editor_de_textos;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 *
 * @author EstMP
 */
public class Editor {

    /**
     *
     */
    public static final String FORM_TITLE = "Editor de textos";

    // DOCUMENTO
    private String currentFilePath;
    private int fontSize;
    private int numColumns;
    private boolean hasChanged;

    // BÚSQUEDA
    private String findText;
    private int findCount;
    private List<Integer> findListIndex;

    // CONTROLES
    private final JTextArea jTextArea1;
    private final JLabel jLabelFind, jLabelStstus;
    private final EditorGUI eGUI;

    /**
     *
     * @param nGUI
     * @param jTextArea1
     * @param jLabelFind
     * @param jLabelStstus
     */
    public Editor(EditorGUI nGUI, JTextArea jTextArea1, JLabel jLabelFind, JLabel jLabelStstus) {
        this.currentFilePath = "Nuevo documento.txt";
        this.findCount = 0;
        this.numColumns = 1;
        this.fontSize = 12;
        this.eGUI = nGUI;
        this.jTextArea1 = jTextArea1;
        this.jLabelFind = jLabelFind;
        this.jLabelStstus = jLabelStstus;
    }

    /**
     * Obtiene la ruta del documento
     *
     * @return
     */
    public String getCurrentFilePath() {
        return currentFilePath;
    }

    /**
     * Establece la ruta del documento
     *
     * @param currentFilePath
     */
    public void setCurrentFilePath(String currentFilePath) {
        this.currentFilePath = currentFilePath;
    }

    /**
     * Obtiene el texto a buscar
     *
     * @return
     */
    public String getFindText() {
        return findText;
    }

    /**
     * Establece el texto a buscar
     *
     * @param findText
     */
    public void setFindText(String findText) {
        this.findText = findText;
    }

    /**
     * Obtiene si se han hecho cambios en el documento
     *
     * @return
     */
    public boolean isHasChanged() {
        return hasChanged;
    }

    /*------------------------------------------------------------------------*/
    /**
     * Nuevo documento
     *
     */
    protected void newDoc() {
        jTextArea1.setText("");
        jLabelStstus.setText("Nuevo archivo");
        this.setCurrentFilePath("Nuevo documento.txt");
        this.update();
        hasChanged = false;
    }

    /**
     * Abrir archivo
     *
     * @param f
     */
    protected void openFile(File f) {
        try {
            readFile(f);
            updateLineCount();

            setCurrentFilePath(f.getPath());

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EditorGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EditorGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* Lee el archivo especificado, línea por línea y devuelve un String con el
    documento formado */
    private void readFile(File f) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        jTextArea1.read(br, null);
        jTextArea1.getDocument().addDocumentListener(eGUI);
        br.close();
        hasChanged = false;

    }

    /**
     * Guardar archivo
     *
     * Guarda el documento actual en la ruta especificada por
     * getCurrentFilePath()
     *
     * @throws java.io.IOException
     */
    protected void writeFile() throws IOException {
        File temp, f;
        boolean rename, delete;
        BufferedWriter writer;

        temp = File.createTempFile("editor-save", ".tmp");
        writer = new BufferedWriter(new FileWriter(temp));
        jTextArea1.write(writer);
        writer.close();

        f = new File(getCurrentFilePath());
        delete = f.delete();
        if (!delete && f.exists()) {
            throw new IOException("Error al escribir el archivo");
        }

        rename = temp.renameTo(f);
        if (!rename) {
            throw new IOException("No se ha renombrado el archivo temporal");
        }

        hasChanged = false;
        eGUI.setTitle(currentFilePath + " | " + FORM_TITLE);
        jLabelStstus.setText("Guardado en " + getCurrentFilePath());
    }

    /**
     * Buscar
     *
     * Crea una lista de Integers con las posiciones o índices del texto a
     * buscar Si se ha establecido texto en el atributo findText se crea la
     * lista y se añaden los índices obtenidos con la expresión regular
     */
    protected void findText() {
        String texto = jTextArea1.getText();
        findListIndex = null;
        findCount = 0;

        if (texto.contains(getFindText())) {
            findListIndex = new ArrayList<>();
            Pattern pattern = Pattern.compile(getFindText());
            Matcher matcher = pattern.matcher(texto);

            while (matcher.find()) {
                findListIndex.add(matcher.start());
            }
            findNext();
        } else {
            jLabelFind.setText("No se encontraron coincidencias");
        }
    }

    /**
     * Buscar siguiente...
     *
     * Si se ha creado la lista en el método findText() se recorre para obtener
     * cada índice de la palabra encontrada para posicionar el cursor
     */
    protected void findNext() {
        if (findListIndex != null) {
            int nCoincidencias = findListIndex.size();

            if (nCoincidencias != findCount) {
                jTextArea1.setCaretPosition(findListIndex.get(findCount));
                jTextArea1.select(jTextArea1.getCaretPosition(), jTextArea1.getCaretPosition() + getFindText().length());

                findCount++;
                String formar = String.format("Coincidencias: %d / %d", findCount, nCoincidencias);
                jLabelFind.setText(formar);
            } else {
                jLabelFind.setText("No hay mas resultados");
                findCount = 0; // Volver a 'buscar siguiente' desde el principio
            }

        }
    }

    /**
     * Tamaño de fuente
     *
     * Aumenta o reduce el tamaño de la fuente de texto según el parámetro
     *
     * @param val
     */
    protected void fontSize(int val) {
        Font f = new Font(Font.SANS_SERIF, 0, fontSize + val);
        jTextArea1.setFont(f);
        fontSize = fontSize + val;
    }

    /**
     * Actualizar
     *
     * Se ejecuta siempre que haya algún cambio en JTextArea
     *
     * 1º Detecta si se han hecho cambios en el documento 2º Actualiza la cuenta
     * de las líneas del documento en cada cambio 3º Comprueba si hay una
     * búsqueda en curso para detenerla
     */
    protected void update() {
        hasChanged = true;
        eGUI.setTitle(getCurrentFilePath() + " (SIN GUARDAR) | " + FORM_TITLE);
        updateLineCount();
        if (findListIndex != null) {
            findListIndex = null;
            findText = null;
            findCount = 0;
            jLabelFind.setText("");
        }
    }

    private void updateLineCount() {
        numColumns = jTextArea1.getLineCount();
        jLabelStstus.setText("Número de líneas: " + String.valueOf(numColumns));
    }

    @Override
    public String toString() {
        return "Editor{" + "currentFilePath=" + currentFilePath + ", \nfontSize=" + fontSize + ", \nnumColumns=" + numColumns + ", \nfindText=" + findText + ", \nfindCount=" + findCount + ", \nfindListIndex=" + findListIndex + ", \njTextArea1=" + jTextArea1 + ", \njLabelFind=" + jLabelFind + ", \njLabelStstus=" + jLabelStstus + ", \neGUI=" + eGUI + '}';
    }

}
