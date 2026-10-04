package avancecurricular.ui.gui;

import avancecurricular.model.Estudiante;
import avancecurricular.model.RegistroAcademico;
import avancecurricular.ui.controller.ControladorEstudiante;
import avancecurricular.ui.view.VistaEstudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collection;

/**
 * Panel gráfico (Swing) para el módulo de Estudiante.
 * Utiliza un {@link DefaultTableModel} sobreescrito para renderizar una tabla de sólo lectura,
 * delegando la reactividad (actualizaciones, registros, eliminaciones) mediante listeners hacia el controlador.
 */
public class GuiVistaEstudiante extends GuiVistaModulo implements VistaEstudiante {

    private ControladorEstudiante controlador;
    private JTextField txtRut;
    private JTextField txtNombre;
    private JTextField txtIdCarrera;

    public GuiVistaEstudiante() {
        super("Registrar Estudiante", 3, 5,
              "Matrícula de Estudiantes", "RUT", "Nombre", "Carrera", "Avance (%)");

        txtRut = agregarCampo("RUT:");
        txtNombre = agregarCampo("Nombre:");
        txtIdCarrera = agregarCampo("ID Carrera:");

        JButton btnVerRegistros = agregarAccion("Ver Expediente Académico");
        JButton btnInscribirCurso = agregarAccion("Inscribir Curso");
        JButton btnCalificar = agregarAccion("Actualizar Registro Curso");
        JButton btnRetirarCurso = agregarAccion("Desinscribir Curso");
        JButton btnEliminar = agregarAccion("Eliminar Estudiante");

        btnAgregar.addActionListener(e -> {
            if (controlador != null) {
                String rut = txtRut.getText().trim();
                String nombre = txtNombre.getText().trim();
                String idCarrera = txtIdCarrera.getText().trim();
                
                if (rut.isEmpty() || nombre.isEmpty() || idCarrera.isEmpty()) {
                    mostrarError("Todos los campos del formulario son obligatorios.");
                    return;
                }
                
                controlador.onAgregarEstudiante(rut, nombre, idCarrera);
                limpiarFormulario();
            }
        });

        btnEliminar.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                     "¿Está seguro que desea eliminar al estudiante con RUT " + rut + "?",
                     "Confirmar Eliminación",
                     JOptionPane.YES_NO_OPTION);
                     
                if (confirmacion == JOptionPane.YES_OPTION) {
                    controlador.onEliminarEstudiante(rut);
                }
            }
        });

        btnVerRegistros.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                controlador.onSolicitarRegistros(rut);
            }
        });

        btnInscribirCurso.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                String idCurso = JOptionPane.showInputDialog(this,
                     "Ingrese el ID del curso a inscribir:",
                     "Inscribir Curso",
                     JOptionPane.QUESTION_MESSAGE);
                     
                if (idCurso != null && !idCurso.trim().isEmpty()) {
                    controlador.onInscribirCurso(rut, idCurso.trim());
                }
            }
        });

        btnRetirarCurso.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                String idCurso = JOptionPane.showInputDialog(this,
                     "Ingrese el ID del curso a desinscribir:",
                     "Retirar Curso",
                     JOptionPane.WARNING_MESSAGE);
                     
                if (idCurso != null && !idCurso.trim().isEmpty()) {
                    controlador.onDesinscribirCurso(rut, idCurso.trim());
                }
            }
        });

        btnCalificar.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                JTextField txtIdCurso = new JTextField();
                JTextField txtNota = new JTextField("0.0");
                JComboBox<String> cmbEstado = new JComboBox<>(new String[]{
                    RegistroAcademico.ESTADO_CURSANDO, 
                    RegistroAcademico.ESTADO_APROBADO, 
                    RegistroAcademico.ESTADO_REPROBADO
                });
                
                Object[] inputs = {
                    "ID del Curso:", txtIdCurso,
                    "Nota obtenida (ej: 5.5, 0.0 si está cursando):", txtNota,
                    "Estado final:", cmbEstado
                };
                
                int option = JOptionPane.showConfirmDialog(this, inputs, "Actualizar Registro Académico", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        String idCurso = txtIdCurso.getText().trim();
                        if (idCurso.isEmpty()) {
                            mostrarError("Debe especificar el ID del curso.");
                            return;
                        }
                        
                        double nota = Double.parseDouble(txtNota.getText().trim().replace(",", "."));
                        String estado = (String) cmbEstado.getSelectedItem();
                        
                        controlador.onActualizarRegistro(rut, idCurso, nota, estado);
                    } catch (NumberFormatException ex) {
                        mostrarError("La nota debe ser un número decimal válido.");
                    }
                }
            }
        });
    }

    /**
     * Extrae de forma segura el identificador de la entidad en la fila seleccionada por el usuario.
     *
     * @return El identificador (RUT) contenido en la columna 0, 
     *         o {@code null} si no hay ninguna fila seleccionada en la tabla.
     */
    private String obtenerRutSeleccionado() {
        return obtenerIdentificadorSeleccionado("Debe seleccionar un estudiante de la tabla para realizar esta acción.");
    }

    private void limpiarFormulario() {
        txtRut.setText("");
        txtNombre.setText("");
        txtIdCarrera.setText("");
        txtRut.requestFocus();
    }

    @Override
    public void setControlador(ControladorEstudiante controlador) {
        this.controlador = controlador;
    }

    @Override
    public void iniciar() {
        if (controlador != null) {
            controlador.onSolicitarListaEstudiantes();
        }
    }

    @Override
    public void mostrarListaEstudiantes(Collection<Estudiante> estudiantes) {
        tableModel.setRowCount(0);
        for (Estudiante estudiante : estudiantes) {
            tableModel.addRow(new Object[]{
                estudiante.getRut(), 
                estudiante.getNombre(), 
                estudiante.getCarrera().getNombre(),
                String.format("%.1f%%", estudiante.calcularPorcentajeAvance())
            });
        }
    }

    @Override
    public void mostrarRegistrosAcademicos(Estudiante estudiante) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Expediente Académico: ").append(estudiante.getNombre()).append(" ===\n");
        sb.append("Carrera: ").append(estudiante.getCarrera().getNombre()).append("\n");
        
        sb.append(String.format("Avance Curricular: %.1f%%\n", estudiante.calcularPorcentajeAvance()));
        sb.append("Créditos Aprobados: ").append(estudiante.obtenerCreditosAprobados())
          .append(" / ").append(estudiante.getCarrera().getCreditosTotales()).append("\n");
        sb.append("--------------------------------------------------\n");
        
        if (estudiante.getRegistrosAcademicos().isEmpty()) {
            sb.append("(No posee registros académicos vigentes)\n");
        } else {
            for (RegistroAcademico registro : estudiante.getRegistrosAcademicos()) {
                sb.append(String.format("- [%s] %s | Nota: %.1f | Estado: %s\n", 
                    registro.getCurso().getId(),
                    registro.getCurso().getNombre(),
                    registro.getNota(),
                    registro.getEstado()
                ));
            }
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        
        Font defaultFont = UIManager.getFont("Label.font");
        int fontSize = (defaultFont != null) ? defaultFont.getSize() : 16;
        textArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Expediente: " + estudiante.getRut(), JOptionPane.INFORMATION_MESSAGE);
    }

}
