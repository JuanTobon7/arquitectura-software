package com.pipesfilters.concurrence;

import com.pipesfilters.dtos.FileFrame;
import com.pipesfilters.filters.Filter;
import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public abstract class ConcurrenceBase implements Filter {

    private final ExecutorService pool =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void process(FpsReader entrada, FpsWriter salida) throws InterruptedException, ExecutionException, IOException {

        BlockingQueue<FileFrame> colaSalida = new LinkedBlockingQueue<>();
        Thread writer = new Thread(() -> drenarHaciaSalida(colaSalida, salida));
        writer.start();

        List<Future<?>> taks = new ArrayList<>();
        FileFrame frame;
        while ((frame = entrada.read()) != null) {
            FileFrame actual = frame;
            taks.add(pool.submit(() -> {
                try {
                    colaSalida.put(processFile(actual));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        for (Future<?> t : taks) t.get();   // esperar a que terminen todos
        colaSalida.put(FileFrame.END);         // cierre para el hilo escritor
        writer.join();
        pool.shutdown();
    }

    protected abstract FileFrame processFile(FileFrame origen) throws Exception;

    private void drenarHaciaSalida(BlockingQueue<FileFrame> queue, FpsWriter out) {
        try {
            FileFrame frame;
            while ((frame = queue.take()) != FileFrame.END) {
                out.write(frame);   // serializa un bloque FPS completo
            }
            out.close();              // flush + EOF para el siguiente filtro
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}