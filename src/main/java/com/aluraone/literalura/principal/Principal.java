package com.aluraone.literalura.principal;

import com.aluraone.literalura.model.*;
import com.aluraone.literalura.repository.AutorRepository;
import com.aluraone.literalura.repository.LibroRepository;
import com.aluraone.literalura.service.ConsumoAPI;
import com.aluraone.literalura.service.ConvierteDatos;

import java.util.Comparator;
import java.util.Optional;
import java.util.Scanner;
import java.util.List;


public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private ConsumoAPI consulta = new ConsumoAPI();
    private int opcionUsuario = -1;
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    List<Autor> autores;
    List<Libro> libros;
    Scanner teclado = new Scanner(System.in);

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu(){

        do{
            mostrarMenuPrincipal();
            opcionUsuario = Integer.valueOf(teclado.nextLine());
            switch(opcionUsuario){
                case 1:
                   buscarLibroWeb();
                    break;
                case 2:
                    mostrarLibrosBuscados();
                    break;
                case 3:
                    mostrarAutoresBuscados();
                    break;
                case 4:
                    mostrarAutoresPorAnio();
                    break;
                case 5:
                    mostrarLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Finalizando el programa, gracias");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } while  (opcionUsuario != 0);


    }

    public void mostrarMenuPrincipal(){
        System.out.println("""
                
                Elija la opción a través de su número:                             
                1- Buscar libro por título
                2- Listar libros registrados
                3- Listar autores registrados
                4- Listar autores vivos en un determinado año
                5- Listar libros por idioma   
                0- SALIR
                """);
    }

    public void mostrarMenuIdionas(){
        System.out.println("""
                Ingrese el idioma para buscar los libros:
                en- inglés
                es- español
                fr- francés
                pt- portugués
                """);
    }

    private void buscarLibroWeb(){
        System.out.println("Ingrese el título del libro que desea buscar");
        String libroUsuario = teclado.nextLine();
        String busqueda = "?search=" + libroUsuario.replace(" ","+");
        var json = consulta.obtenerDatos(URL_BASE + busqueda);
//        System.out.println(json); mostrar para comprobar que salió bien
        var datos = conversor.obtenerDatos(json, Datos.class);

        if (!datos.resultados().isEmpty()){
            DatosLibros datoslibro = datos.resultados().get(0);
            Libro libro = new Libro(datoslibro);
            Autor autor = new Autor().obtenerPrimerAutor(datoslibro);
            System.out.println(libro);
            guardarLibroConAutor(libro, autor); //guarda el libro y el autor en caso de no existir en la DB
        } else{
            System.out.println("el libro no existe en Gutendex.com");
        }
    }

    private void guardarLibroConAutor(Libro libro, Autor autor){
        Optional<Libro> libroBuscado = libroRepository.findByTituloContains(libro.getTitulo());
        if(libroBuscado.isPresent()){
            System.out.println("El libro ya existe en la base de datos");
            libro.setTitulo(String.valueOf(libroBuscado.get()));
        } else {
            System.out.println("Nuevo libro ingresado");
            libroRepository.save(libro);
            libro.setAutor(autor);
        }

        Optional<Autor> autorBuscado = autorRepository.findByNombreContains(autor.getNombre());
        if(autorBuscado.isPresent()){
            System.out.println("El autor ya existe en la base de datos");
            libro.setAutor(autorBuscado.get());
        } else {
            System.out.println("Nuevo autor ingresado");
            autorRepository.save(autor);
            libro.setAutor(autor);
        }
    }

    private void mostrarLibrosBuscados() {
        libros = libroRepository.findAll();
        imprimeLibrosOrdenadosPorNombre(libros);
    }


    private void mostrarAutoresBuscados() {
        autores = autorRepository.findAll();
        imprimeAutoresOrdenadosPorNombre(autores);
    }

    private void mostrarAutoresPorAnio(){
        System.out.println("Ingresa el año para ver los autores vivos de esa época");
        Integer anio = Integer.valueOf(teclado.nextLine());

        autores = autorRepository
                .findByFechaDeNacimientoLessThanEqualAndFechaDeMuerteGreaterThanEqual
                        (anio, anio);
        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos para el año ingresado");
        } else {
            imprimeAutoresOrdenadosPorNombre(autores);
        }
    }

    private void imprimeAutoresOrdenadosPorNombre(List<Autor> autores){
        autores.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(System.out::println);
    }

    private void imprimeLibrosOrdenadosPorNombre(List<Libro> libros) {
        libros.stream()
                .sorted(Comparator.comparing(Libro::getNombreAutor))
                .forEach(System.out::println);
    }

    private void mostrarLibrosPorIdioma(){
        mostrarMenuIdionas();
        String idioma = teclado.nextLine();

        String claveIdioma;
        if (idioma.length() >= 2) {
            claveIdioma = idioma.substring(0, 2);
        } else {
            claveIdioma = idioma;
        }

        libros = libroRepository.findByIdiomasContains(claveIdioma);

        if (libros.isEmpty()) {
            System.out.println("No se encontraron libros para el idioma ingresado");
        } else {
            imprimeLibrosOrdenadosPorNombre(libros);
        }

    }

}
