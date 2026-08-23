package mathtexpedia.es.api.domain.model.pdf;

public enum PDFTag {

    AC {
        @Override
        public String getName(){
            return "Análisis y Cálculo";
        }
    },
    AG {
        @Override
        public String getName() {
            return "Álgebra y Geometría";
        }
    },
    TE {
        @Override
        public String getName() {
            return "Topología y Estructuras Matemáticas";
        }
    },
    EM {
        @Override
        public String getName() {
            return "Ecuaciones Diferenciales y Métodos Numéricos";
        }
    },
    PE {
        @Override
        public String getName() {
            return "Probabilidad y Estadística";
        }
    },
    OP {
        @Override
        public String getName() {
            return "Optimización y Programación Matemática";
        }
    },
    FA {
        @Override
        public String getName() {
            return "Fundamentos de Programación y Algoritmia";
        }
    },
    EL {
        @Override
        public String getName() {
            return "Estructuras, Computabilidad y Lenguajes";
        }
    },
    AS {
        @Override
        public String getName() {
            return "Arquitectura y Sistemas";
        }
    },
    IP {
        @Override
        public String getName() {
            return "Ingeniería del Software y Proyectos";
        }
    },
    BD {
        @Override
        public String getName() {
            return "Base de Datos";
        }
    },
    WI {
        @Override
        public String getName() {
            return "Web e Interfaces";
        }
    },
    SI {
        @Override
        public String getName() {
            return "Seguridad e IA";
        }
    };

    public abstract String getName();
}
