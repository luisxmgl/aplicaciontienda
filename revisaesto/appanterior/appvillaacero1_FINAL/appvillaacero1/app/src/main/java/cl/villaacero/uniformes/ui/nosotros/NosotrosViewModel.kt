package cl.villaacero.uniformes.ui.nosotros

import androidx.lifecycle.ViewModel
import cl.villaacero.uniformes.data.CatalogRepository
import cl.villaacero.uniformes.data.Colegio
import cl.villaacero.uniformes.data.Negocio

class NosotrosViewModel : ViewModel() {

    val negocio: Negocio = CatalogRepository.negocio()
    val colegios: List<Colegio> = CatalogRepository.colegios().sortedBy { it.nombre }
    val comunas: List<String> = CatalogRepository.comunas()
    val totalProductos: Int = CatalogRepository.totalProductos()
}
