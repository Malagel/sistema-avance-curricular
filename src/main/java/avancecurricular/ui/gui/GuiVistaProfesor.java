package avancecurricular.ui.gui;

import avancecurricular.model.Curso;
import avancecurricular.model.Profesor;
import avancecurricular.ui.controller.ControladorProfesor;
import avancecurricular.ui.view.VistaProfesor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Collection;

/**
 * Panel gráfico (Swing) para el módulo de Profesor.
 * Utiliza un {@link DefaultTableModel} sobreescrito para renderizar una tabla de sólo lectura,
 * delegando la reactividad (actualizaciones, registros, eliminaciones) mediante listeners hacia el controlador.
 */
public class GuiVistaProfesor extends GuiVistaModulo implements VistaProfesor {
    private ControladorProfesor controlador;
    private JTextField txtRut;
    private JTextField txtNombre;

    public GuiVistaProfesor() {
        super("Registrar Profesor", 2, 4,
              "Nómina de Profesores", "RUT", "Nombre", "Cursos Asignados");

        txtRut = agregarCampo("RUT:");
        txtNombre = agregarCampo("Nombre:");

        JButton btnVerCursos = agregarAccion("Ver Cursos Dictados");
        JButton btnAsignarCurso = agregarAccion("Asignar Curso a Profesor");
        JButton btnRemoverCurso = agregarAccion("Remover Curso de Profesor");
        JButton btnEliminar = agregarAccion("Eliminar Profesor");

        btnAgregar.addActionListener(e -> {
            if (controlador != null) {
                String rut = txtRut.getText().trim();
                String nombre = txtNombre.getText().trim();
                
                if (rut.isEmpty() || nombre.isEmpty()) {
                    mostrarError("El RUT y el Nombre no pueden estar vacíos.");
                    return;
                }
                
                controlador.onAgregarProfesor(rut, nombre);
                limpiarFormulario();
            }
        });

        btnEliminar.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                     "¿Está seguro que desea eliminar al profesor con RUT " + rut + "?",
                     "Confirmar Eliminación",
                     JOptionPane.YES_NO_OPTION);
                     
                if (confirmacion == JOptionPane.YES_OPTION) {
                    controlador.onEliminarProfesor(rut);
                }
            }
        });

        btnVerCursos.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                controlador.onSolicitarCursosProfesor(rut);
            }
        });

        btnAsignarCurso.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                String idCurso = JOptionPane.showInputDialog(this,
                     "Ingrese el ID del curso a asignar:",
                     "Asignar Curso",
                     JOptionPane.QUESTION_MESSAGE);
                     
                if (idCurso != null && !idCurso.trim().isEmpty()) {
                    controlador.onAsignarCurso(rut, idCurso.trim());
                }
            }
        });

        btnRemoverCurso.addActionListener(e -> {
            String rut = obtenerRutSeleccionado();
            if (rut != null && controlador != null) {
                String idCurso = JOptionPane.showInputDialog(this,
                     "Ingrese el ID del curso a remover:",
                     "Remover Curso",
                     JOptionPane.WARNING_MESSAGE);
                     
                if (idCurso != null && !idCurso.trim().isEmpty()) {
                    controlador.onRemoverCurso(rut, idCurso.trim());
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
        return obtenerIdentificadorSeleccionado("Debe seleccionar un profesor de la tabla para realizar esta acción.");
    }

    private void limpiarFormulario() {
        txtRut.setText("");
        txtNombre.setText("");
        txtRut.requestFocus();
    }

    @Override
    public void setControlador(ControladorProfesor controlador) {
        this.controlador = controlador;
    }

    @Override
    public void iniciar() {
        if (controlador != null) {
            controlador.onSolicitarListaProfesores();
        }
    }

    @Override
    public void mostrarListaProfesores(Collection<Profesor> profesores) {
        tableModel.setRowCount(0);
        for (Profesor profesor : profesores) {
            tableModel.addRow(new Object[]{
                profesor.getRut(), 
                profesor.getNombre(), 
                profesor.getCursosDictados().size()
            });
        }
    }

    @Override
    public void mostrarCursosDelProfesor(Profesor profesor) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Cursos dictados por ").append(profesor.getNombre()).append(" ===\n\n");
        
        if (profesor.getCursosDictados().isEmpty()) {
            sb.append("(No dicta ningún curso actualmente)\n");
        } else {
            for (Curso curso : profesor.getCursosDictados()) {
                sb.append(" - [").append(curso.getId()).append("] ")
                  .append(curso.getNombre()).append(" (")
                  .append(curso.getCreditos()).append(" créditos)\n");
            }
        }
        
        JOptionPane.showMessageDialog(this, sb.toString(), "Carga Académica: " + profesor.getRut(), JOptionPane.INFORMATION_MESSAGE);
    }

}
