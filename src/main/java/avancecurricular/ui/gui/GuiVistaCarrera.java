package avancecurricular.ui.gui;

import avancecurricular.model.AsignaturaMalla;
import avancecurricular.model.Carrera;
import avancecurricular.ui.controller.ControladorCarrera;
import avancecurricular.ui.view.VistaCarrera;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Panel gráfico (Swing) para el módulo de Carrera.
 * Utiliza un {@link DefaultTableModel} sobreescrito para renderizar una tabla de sólo lectura,
 * delegando la reactividad (actualizaciones, registros, eliminaciones) mediante listeners hacia el controlador.
 */
public class GuiVistaCarrera extends GuiVistaModulo implements VistaCarrera {

    private ControladorCarrera controlador;
    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtCreditos;

    public GuiVistaCarrera() {
        super("Registrar Carrera", 3, 4,
              "Catálogo de Carreras", "ID", "Nombre", "Créditos Totales", "Cant. Asignaturas");

        txtId = agregarCampo("ID:");
        txtNombre = agregarCampo("Nombre:");
        txtCreditos = agregarCampo("Créditos:");

        JButton btnVerMalla = agregarAccion("Ver Malla Curricular");
        JButton btnAddAsignatura = agregarAccion("Agregar Curso a la Malla");
        JButton btnAddPrerrequisito = agregarAccion("Agregar Prerrequisito a Curso");
        JButton btnEliminar = agregarAccion("Eliminar Carrera");

        btnAgregar.addActionListener(e -> {
            if (controlador != null) {
                try {
                    String id = txtId.getText().trim();
                    String nombre = txtNombre.getText().trim();
                    int creditos = Integer.parseInt(txtCreditos.getText().trim());
                    
                    if (id.isEmpty() || nombre.isEmpty()) {
                        mostrarError("El ID y el Nombre no pueden estar vacíos.");
                        return;
                    }
                    
                    controlador.onAgregarCarrera(id, nombre, creditos);
                    limpiarFormulario();
                } catch (NumberFormatException ex) {
                    mostrarError("Los créditos totales deben ser un número entero válido.");
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            String idCarrera = obtenerIdSeleccionado();
            if (idCarrera != null && controlador != null) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                     "¿Está seguro que desea eliminar la carrera " + idCarrera + "?",
                     "Confirmar Eliminación",
                     JOptionPane.YES_NO_OPTION);
                     
                if (confirmacion == JOptionPane.YES_OPTION) {
                    controlador.onEliminarCarrera(idCarrera);
                }
            }
        });

        btnVerMalla.addActionListener(e -> {
            String idCarrera = obtenerIdSeleccionado();
            if (idCarrera != null && controlador != null) {
                controlador.onVerDetalleMalla(idCarrera);
            }
        });

        btnAddAsignatura.addActionListener(e -> {
            String idCarrera = obtenerIdSeleccionado();
            if (idCarrera != null && controlador != null) {
                JTextField txtIdCurso = new JTextField();
                JTextField txtSemestre = new JTextField();
                Object[] inputs = {
                    "ID del Curso a integrar:", txtIdCurso,
                    "Semestre en el que se dictará:", txtSemestre
                };
                
                int option = JOptionPane.showConfirmDialog(this, inputs, "Agregar Asignatura", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        String idCurso = txtIdCurso.getText().trim();
                        int semestre = Integer.parseInt(txtSemestre.getText().trim());
                        controlador.onAgregarAsignaturaMalla(idCarrera, idCurso, semestre);
                    } catch (NumberFormatException ex) {
                        mostrarError("El semestre debe ser un número entero.");
                    }
                }
            }
        });

        btnAddPrerrequisito.addActionListener(e -> {
            String idCarrera = obtenerIdSeleccionado();
            if (idCarrera != null && controlador != null) {
                JTextField txtDestino = new JTextField();
                JTextField txtPre = new JTextField();
                Object[] inputs = {
                    "ID del Curso que requiere prerrequisito:", txtDestino,
                    "ID del Curso que DEBE SER APROBADO PREVIAMENTE:", txtPre
                };
                
                int option = JOptionPane.showConfirmDialog(this, inputs, "Agregar Prerrequisito", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String idDestino = txtDestino.getText().trim();
                    String idPre = txtPre.getText().trim();
                    controlador.onAgregarPrerrequisito(idCarrera, idDestino, idPre);
                }
            }
        });
    }

    /**
     * Extrae de forma segura el identificador de la entidad en la fila seleccionada por el usuario.
     *
     * @return El identificador (ej. ID o RUT) contenido en la columna 0,
     *         o {@code null} si no hay ninguna fila seleccionada en la tabla.
     */
    private String obtenerIdSeleccionado() {
        return obtenerIdentificadorSeleccionado("Debe seleccionar una carrera de la tabla para realizar esta acción.");
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtCreditos.setText("");
        txtId.requestFocus();
    }

    @Override
    public void setControlador(ControladorCarrera controlador) {
        this.controlador = controlador;
    }

    @Override
    public void iniciar() {
        if (controlador != null) {
            controlador.onSolicitarListaCarreras();
        }
    }

    @Override
    public void mostrarListaCarreras(Collection<Carrera> carreras) {
        tableModel.setRowCount(0);
        for (Carrera carrera : carreras) {
            tableModel.addRow(new Object[]{
                carrera.getId(),
                carrera.getNombre(),
                carrera.getCreditosTotales(),
                carrera.getPlanDeEstudio().size()
            });
        }
    }

    @Override
    public void mostrarDetalleMalla(Carrera carrera) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Malla Curricular: ").append(carrera.getNombre()).append(" ===\n\n");
        
        if (carrera.getPlanDeEstudio().isEmpty()) {
            sb.append("(No hay asignaturas registradas en esta malla)\n");
        } else {
            List<AsignaturaMalla> mallaOrdenada = new ArrayList<>(carrera.getPlanDeEstudio());
            mallaOrdenada.sort(Comparator.comparingInt(AsignaturaMalla::getNumeroSemestre));
            
            int semestreActual = -1;
            for (AsignaturaMalla am : mallaOrdenada) {
                if (am.getNumeroSemestre() != semestreActual) {
                    semestreActual = am.getNumeroSemestre();
                    sb.append("\n[ Semestre ").append(semestreActual).append(" ]\n");
                }
                sb.append("  - ").append(am.getCurso().getId()).append(": ").append(am.getCurso().getNombre());
                
                if (!am.getPrerrequisitos().isEmpty()) {
                    sb.append("\n      (Prerrequisitos: ");
                    List<String> pres = new ArrayList<>();
                    am.getPrerrequisitos().forEach(p -> pres.add(p.getId()));
                    sb.append(String.join(", ", pres)).append(")");
                }
                sb.append("\n");
            }
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        
        Font defaultFont = UIManager.getFont("Label.font");
        int fontSize = (defaultFont != null) ? defaultFont.getSize() : 16;
        textArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Detalle de Malla: " + carrera.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

}
