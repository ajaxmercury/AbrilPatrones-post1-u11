package com.universidad.refactoringu11.inventario;

import com.universidad.refactoringu11.reportes.ReporteBajoStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventarioService {

    private final List<Producto> productos = new ArrayList<>();
    private final List<String> movimientos = new ArrayList<>();

    private ReporteBajoStockService reporteBajoStockService;

    @Autowired(required = false)
    public void setReporteBajoStockService(ReporteBajoStockService reporteBajoStockService) {
        this.reporteBajoStockService = reporteBajoStockService;
    }

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public String actualizarInventario(Long productoId, int cantidad, String tipoMovimiento) {
        if (sonDatosInvalidos(productoId, cantidad, tipoMovimiento)) return "ERROR_DATOS_INVALIDOS";
        Producto producto = buscarProducto(productoId);
        if (producto == null) return "ERROR_PRODUCTO_NO_EXISTE";
        if (esStockInsuficiente(producto, cantidad, tipoMovimiento)) return "ERROR_STOCK_INSUFICIENTE";
        int nuevoStock = aplicarMovimientoStock(producto, cantidad, tipoMovimiento);
        int cantidadReorden = calcularCantidadReorden(producto, nuevoStock);
        registrarMovimiento(tipoMovimiento, cantidad, producto, nuevoStock);
        return procesarNotificacion(producto, nuevoStock, cantidadReorden);
    }

    private boolean sonDatosInvalidos(Long productoId, int cantidad, String tipoMovimiento) {
        return productoId == null || cantidad == 0
                || (!tipoMovimiento.equals("ENTRADA") && !tipoMovimiento.equals("SALIDA"));
    }

    private Producto buscarProducto(Long productoId) {
        for (Producto p : productos) {
            if (p.getId().equals(productoId)) return p;
        }
        return null;
    }

    private boolean esStockInsuficiente(Producto producto, int cantidad, String tipoMovimiento) {
        return tipoMovimiento.equals("SALIDA") && (producto.getStock() - cantidad < 0);
    }

    private int aplicarMovimientoStock(Producto producto, int cantidad, String tipoMovimiento) {
        int nuevoStock = tipoMovimiento.equals("ENTRADA")
                ? producto.getStock() + cantidad
                : producto.getStock() - cantidad;
        producto.setStock(nuevoStock);
        return nuevoStock;
    }

    private int calcularCantidadReorden(Producto producto, int nuevoStock) {
        boolean necesitaReorden = nuevoStock < producto.getStockMinimo();
        return necesitaReorden ? (producto.getStockMinimo() * 3) - nuevoStock : 0;
    }

    private void registrarMovimiento(String tipoMovimiento, int cantidad, Producto producto, int nuevoStock) {
        String registro = tipoMovimiento + " de " + cantidad + " unidades de "
                + producto.getNombre() + " -> stock final: " + nuevoStock;
        movimientos.add(registro);
        System.out.println(registro);
    }

    private String procesarNotificacion(Producto producto, int nuevoStock, int cantidadReorden) {
        if (nuevoStock < producto.getStockMinimo()) {
            if (reporteBajoStockService != null) {
                System.out.println(reporteBajoStockService.generarReporte());
            }
            System.out.println("ALERTA: " + producto.getNombre()
                    + " bajo stock minimo. Reordenar " + cantidadReorden + " unidades.");
            return "OK_BAJO_STOCK_REORDEN_" + cantidadReorden;
        }
        return "OK_" + nuevoStock;
    }
}
