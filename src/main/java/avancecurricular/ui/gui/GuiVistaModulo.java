package avancecurricular.ui.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Plantilla común de los módulos: formulario, acciones y tabla filtrable.
 * Las vistas concretas definen sus campos y conservan sus listeners y controladores.
 */
public abstract class GuiVistaModulo extends JPanel {
    protected final DefaultTableModel tableModel;
    protected final JTable tabla;
    protected final JButton btnAgregar;
    private final JPanel panelCampos;
    private final JPanel panelBotones;

    protected GuiVistaModulo(String tituloFormulario, int cantidadCampos, int cantidadAcciones,
                             String tituloTabla, String... columnas) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelIzquierdo = new JPanel(new BorderLayout(0, 20));
        panelIzquierdo.setPreferredSize(new Dimension(350, 0));

        JPanel panelFormulario = new JPanel(new BorderLayout(0, 15));
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(tituloFormulario),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panelCampos = new JPanel(new GridLayout(cantidadCampos, 2, 10, 15));
        btnAgregar = crearBoton("Registrar");
        btnAgregar.setPreferredSize(new Dimension(0, 45));
        panelFormulario.add(panelCampos, BorderLayout.CENTER);
        panelFormulario.add(btnAgregar, BorderLayout.SOUTH);

        panelBotones = new JPanel(new GridLayout(cantidadAcciones, 1, 0, 10));
        panelBotones.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Acciones"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panelIzquierdo.add(panelFormulario, BorderLayout.NORTH);
        panelIzquierdo.add(panelBotones, BorderLayout.CENTER);
        add(panelIzquierdo, BorderLayout.WEST);

        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(tableModel);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.setRowHeight(35);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tabla.setShowVerticalLines(false);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(tituloTabla),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(rowSorter);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelBusqueda.add(new JLabel("Filtrar:"));

        JTextField txtBuscar = new JTextField(20);
        panelBusqueda.add(txtBuscar);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrarTabla(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrarTabla(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrarTabla(); }

            private void filtrarTabla() {
                String texto = txtBuscar.getText();
                if (texto.trim().isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
                }
            }
        });

        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(panelBusqueda, BorderLayout.NORTH);
        panelCentral.add(scrollPane, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);
    }

    protected final JTextField agregarCampo(String etiqueta) {
        panelCampos.add(new JLabel(etiqueta));
        JTextField campo = new JTextField();
        panelCampos.add(campo);
        return campo;
    }

    protected final JButton agregarAccion(String titulo) {
        JButton boton = crearBoton(titulo);
        panelBotones.add(boton);
        return boton;
    }

    private static JButton crearBoton(String titulo) {
        return new JButton("<html><p style='text-align:center;'>" + titulo + "</p></html>");
    }

    protected final String obtenerIdentificadorSeleccionado(String error) {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            int filaModelo = tabla.convertRowIndexToModel(fila);
            return (String) tableModel.getValueAt(filaModelo, 0);
        } else {
            mostrarError(error);
            return null;
        }
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }
}