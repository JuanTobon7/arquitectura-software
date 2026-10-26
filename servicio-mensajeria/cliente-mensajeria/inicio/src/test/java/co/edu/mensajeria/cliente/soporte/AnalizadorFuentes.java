package co.edu.mensajeria.cliente.soporte;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Analiza {@code src/main/java} de todos los módulos con el árbol sintáctico del compilador
 * (API {@code com.sun.source}, incluida en el JDK), como hace el servidor. Obtiene el tamaño y
 * la complejidad de cada clase y cada método sin herramientas externas.
 */
public final class AnalizadorFuentes {

    /** Un tipo declarado. {@code record} indica si es un record de datos. */
    public record Tipo(String nombre, boolean record, int lineas, int metodosPublicos) {
    }

    public record Metodo(String clase, String nombre, int lineas, int complejidad, int parametros, boolean deRecord) {
        @Override
        public String toString() {
            return clase + "." + nombre;
        }
    }

    private static AnalizadorFuentes instancia;

    private final List<Tipo> tipos = new ArrayList<>();
    private final List<Metodo> metodos = new ArrayList<>();
    private final List<String> instanceOf = new ArrayList<>();

    public static synchronized AnalizadorFuentes instancia() {
        if (instancia == null) {
            instancia = new AnalizadorFuentes();
        }
        return instancia;
    }

    private AnalizadorFuentes() {
        JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager archivos = compilador.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            JavacTask tarea = (JavacTask) compilador.getTask(null, archivos, null, List.of("-proc:none"), null,
                    archivos.getJavaFileObjectsFromPaths(FuentesDelProyecto.archivosMain()));
            SourcePositions posiciones = Trees.instance(tarea).getSourcePositions();
            for (CompilationUnitTree unidad : tarea.parse()) {
                new Recolector(unidad, posiciones).scan(unidad, null);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Tipo> tipos() {
        return tipos;
    }

    public List<Metodo> metodos() {
        return metodos;
    }

    public List<String> usosDeInstanceOf() {
        return instanceOf;
    }

    /** Recorre el árbol sintáctico y llena las listas. */
    private final class Recolector extends TreePathScanner<Void, Void> {

        private final CompilationUnitTree unidad;
        private final SourcePositions posiciones;
        private final LineMap lineas;
        private final Deque<ClassTree> clases = new ArrayDeque<>();
        private final Deque<int[]> complejidades = new ArrayDeque<>();

        Recolector(CompilationUnitTree unidad, SourcePositions posiciones) {
            this.unidad = unidad;
            this.posiciones = posiciones;
            this.lineas = unidad.getLineMap();
        }

        private int lineas(Tree t) {
            long inicio = posiciones.getStartPosition(unidad, t);
            long fin = posiciones.getEndPosition(unidad, t);
            return (int) (lineas.getLineNumber(fin) - lineas.getLineNumber(inicio) + 1);
        }

        private String claseActual() {
            return clases.isEmpty() ? "?" : clases.peek().getSimpleName().toString();
        }

        @Override
        public Void visitClass(ClassTree c, Void v) {
            if (c.getSimpleName().isEmpty()) {
                return super.visitClass(c, v); // clase anónima: cuenta dentro del método que la contiene
            }
            boolean interfaz = c.getKind() == Tree.Kind.INTERFACE;
            long publicos = c.getMembers().stream()
                    .filter(m -> m instanceof MethodTree metodo && metodo.getReturnType() != null
                            && (interfaz || metodo.getModifiers().getFlags().contains(Modifier.PUBLIC)))
                    .count();
            tipos.add(new Tipo(c.getSimpleName().toString(), c.getKind() == Tree.Kind.RECORD, lineas(c), (int) publicos));
            clases.push(c);
            super.visitClass(c, v);
            clases.pop();
            return null;
        }

        @Override
        public Void visitMethod(MethodTree m, Void v) {
            complejidades.push(new int[]{1});
            super.visitMethod(m, v);
            int complejidad = complejidades.pop()[0];
            if (m.getBody() != null) {
                boolean deRecord = !clases.isEmpty() && clases.peek().getKind() == Tree.Kind.RECORD;
                metodos.add(new Metodo(claseActual(), m.getReturnType() == null ? "<init>" : m.getName().toString(),
                        lineas(m), complejidad, m.getParameters().size(), deRecord));
            }
            return null;
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree t, Void v) {
            instanceOf.add(claseActual() + ": " + t);
            return super.visitInstanceOf(t, v);
        }

        private void sumar() {
            if (!complejidades.isEmpty()) {
                complejidades.peek()[0]++;
            }
        }

        @Override
        public Void visitIf(IfTree t, Void v) { sumar(); return super.visitIf(t, v); }

        @Override
        public Void visitForLoop(ForLoopTree t, Void v) { sumar(); return super.visitForLoop(t, v); }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree t, Void v) { sumar(); return super.visitEnhancedForLoop(t, v); }

        @Override
        public Void visitWhileLoop(WhileLoopTree t, Void v) { sumar(); return super.visitWhileLoop(t, v); }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree t, Void v) { sumar(); return super.visitDoWhileLoop(t, v); }

        @Override
        public Void visitCatch(CatchTree t, Void v) { sumar(); return super.visitCatch(t, v); }

        @Override
        public Void visitConditionalExpression(ConditionalExpressionTree t, Void v) {
            sumar();
            return super.visitConditionalExpression(t, v);
        }

        @Override
        public Void visitCase(CaseTree t, Void v) {
            if (!t.getExpressions().isEmpty()) {
                sumar();
            }
            return super.visitCase(t, v);
        }

        @Override
        public Void visitBinary(BinaryTree t, Void v) {
            if (t.getKind() == Tree.Kind.CONDITIONAL_AND || t.getKind() == Tree.Kind.CONDITIONAL_OR) {
                sumar();
            }
            return super.visitBinary(t, v);
        }
    }
}
