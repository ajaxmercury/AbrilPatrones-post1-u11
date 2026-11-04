package com.universidad.refactoringu11.reportes;

import com.universidad.refactoringu11.inventario.InventarioService;
import com.universidad.refactoringu11.inventario.NotificadorBajoStock;
import com.universidad.refactoringu11.inventario.Producto;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteBajoStockService implements NotificadorBajoStock {

    private final InventarioService inventarioService;
    private final List<String> alertas = new ArrayList<>();

    public ReporteBajoStockService(@Lazy InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @Override
    public void notificar(Producto producto, int cantidadReorden) {
        alertas.add("Producto bajo stock: " + producto.getNombre() + " (Reordenar: " + cantidadReorden + ")");
    }

    public String generarReporte() {
        if (alertas.isEmpty()) {
            return "No hay productos bajo stock minimo.";
        }
        return String.join("\n", alertas);
    }
}
