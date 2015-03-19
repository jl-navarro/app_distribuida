Para ejecuar la aplicación siga los siguientes pasos:
    1. Abra una consola y vaya al directorio ficherormi/target/classes
    2. Ejecute el siguiente comando (consola unix): rmiregistry 2005
    3. Abra otra consola en el mismo directorio
    4. Ejecute el servidor. Para ello, ponga el siguiente comando:
        java -Djava.rmi.server.codebase=file://<ruta>/ficherormi/target/classes -Djava.security.policy=file://<ruta>/ficherormi/target/classes/server.policy -cp . Servidor
    Sustituya <ruta> por el directorio anterior del que se encuentra el directorio ficherormi
    5. Abra otra consola en el mismo directorio
    6. Ejecute el cliente. Para ello, ponga el siguiente comando:
        java -Djava.rmi.server.codebase=file://<ruta>/ficherormi/target/classes -Djava.security.policy=file://<ruta>/ficherormi/target/classes/server.policy -cp . Cliente
    Sustituya <ruta> por el directorio anterior del que se encuentra el directorio ficherormi
