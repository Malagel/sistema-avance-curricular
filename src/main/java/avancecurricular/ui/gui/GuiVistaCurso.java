package avancecurricular.ui.gui;

import avancecurricular.model.Carrera;
import avancecurricular.model.Curso;
import avancecurricular.model.Profesor;
import avancecurricular.ui.controller.ControladorCurso;
import avancecurricular.ui.view.VistaCurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Collection;

/**
 * Panel gráfico (Swing) para el módulo de Cursos.
 * Utiliza un {@link DefaultTableModel} sobreescrito para renderizar una tabla de sólo lectura,
 * delegando la reactividad (actualizaciones, registros, eliminaciones) mediante listeners hacia el controlador.
 */
public class GuiVistaCurso extends GuiVistaModulo implements VistaCurso {
    private ControladorCurso controlador;
    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtCreditos;

    public GuiVistaCurso() {
        super("Registrar Curso", 3, 3,
              "Catálogo de Cursos", "ID", "Nombre", "Créditos");

        txtId = agregarCampo("ID:");
        txtNombre = agregarCampo("Nombre:");
        txtCreditos = agregarCampo("Créditos:");

        JButton btnDetalles = agregarAccion("Ver Detalles");
        JButton btnEliminar = agregarAccion("Eliminar Curso");
        JButton btnModificar = agregarAccion("Modificar Curso");

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
                    
                    controlador.onAgregarCurso(id, nombre, creditos);
                    limpiarFormulario();
                } catch (NumberFormatException ex) {
                    mostrarError("Los créditos deben ser un número entero válido.");
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            String id = obtenerIdSeleccionado();
            if (id != null && controlador != null) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Está seguro que desea eliminar el curso " + id + "?",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION);
                
                if (confirmacion == JOptionPane.YES_OPTION) {
                    controlador.onEliminarCurso(id);
                }
            }
        });

        btnDetalles.addActionListener(e -> {
            String id = obtenerIdSeleccionado();
            if (id != null && controlador != null) {
                controlador.onConsultarDetalleCurso(id);
            }
        });

        btnModificar.addActionListener(e -> {
            String id = obtenerIdSeleccionado();
            if (id != null && controlador != null) {
                JTextField txtNombreDialog = new JTextField();
                JTextField txtCreditosDialog = new JTextField();
                Object[] inputs = {
                    "Nuevo Nombre:", txtNombreDialog,
                    "Nuevos Créditos:", txtCreditosDialog
                };
                
                int option = JOptionPane.showConfirmDialog(this, inputs, "Modificar Curso: " + id, JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        String nombre = txtNombreDialog.getText().trim();
                        int creditos = Integer.parseInt(txtCreditosDialog.getText().trim());
                        
                        if (nombre.isEmpty()) {
                            mostrarError("El nombre no puede estar vacío.");
                            return;
                        }
                        
                        controlador.onModificarCurso(id, nombre, creditos);
                        controlador.onSolicitarListaCursos();
                    } catch (NumberFormatException ex) {
                        mostrarError("Los créditos deben ser un número entero válido.");
                    }
                }
            }
        });
    }

    /**
     * Extrae de forma segura el identificador del curso en la fila seleccionada por el usuario.
     *
     * @return El ID del curso contenido en la columna 0, 
     *         o {@code null} si no hay ninguna fila seleccionada en la tabla.
     */
    private String obtenerIdSeleccionado() {
        return obtenerIdentificadorSeleccionado("Debe seleccionar un curso de la tabla para realizar esta acción.");
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtCreditos.setText("");
        txtId.requestFocus();
    }

    @Override
    public void setControlador(ControladorCurso controlador) {
        this.controlador = controlador;
    }

    @Override
    public void iniciar() {
        if (controlador != null) {
            controlador.onSolicitarListaCursos();
        }
    }

    @Override
    public void mostrarListaCursos(Collection<Curso> cursos) {
        tableModel.setRowCount(0);
        for (Curso curso : cursos) {
            tableModel.addRow(new Object[]{
                curso.getId(), 
                curso.getNombre(), 
                curso.getCreditos()
            });
        }
    }

    @Override
    public void mostrarDetalleCurso(Curso curso, Collection<Carrera> carreras, Collection<Profesor> profesores) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FICHA DEL CURSO ===\n");
        sb.append("ID: ").append(curso.getId()).append("\n");
        sb.append("Nombre: ").append(curso.getNombre()).append("\n");
        sb.append("Créditos: ").append(curso.getCreditos()).append("\n");
        sb.append("---------------------------------\n");
        
        sb.append("Carreras que lo incluyen en su malla:\n");
        if (carreras.isEmpty()) {
            sb.append("  (Ninguna carrera incluye este curso aún)\n");
        } else {
            for (Carrera c : carreras) {
                sb.append("  - ").append(c.getNombre()).append("\n");
            }
        }
        
        sb.append("---------------------------------\n");
        sb.append("Profesores asignados para dictarlo:\n");
        if (profesores.isEmpty()) {
            sb.append("  (Sin profesores asignados)\n");
        } else {
            for (Profesor p : profesores) {
                sb.append("  - ").append(p.getNombre()).append(" (RUT: ").append(p.getRut()).append(")\n");
            }
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Detalles del Curso: " + curso.getId(), JOptionPane.INFORMATION_MESSAGE);
    }

}
