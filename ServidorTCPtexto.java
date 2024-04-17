import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServidorTCPtexto {
    public static void main(String[] args) throws Exception {
        int porta = -1;

        if (args.length < 1) {
            System.out.println("Uso: java ServidorTCPtexto <porta>");
            System.exit(1);
        }

        try {
            porta = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.err.println("Erro: " + e.toString());
            System.exit(1);
        }

        if (porta < 1024) {
            System.err.println("A porta deve ser maior que 1024.");
            System.exit(1);
        }

        ServerSocket servidor = null;
        Socket cliente = null;

        try {

            servidor = new ServerSocket(porta, 10);
            System.out.println("Servidor ouvindo a porta " + porta);
            while (true) {
                // Ficará bloqueado aqui até algum cliente se conectar
                cliente = servidor.accept();
                String ipCliente = cliente.getInetAddress().getHostAddress();
                int portaCliente = cliente.getPort();
                System.out.println("Conexao estabelecida com o cliente " + ipCliente + ", porta " + portaCliente);
                Conexao conexao = new Conexao(cliente);
            }
        } catch (Exception e) {
            System.err.println("Erro: " + e.toString());
        } finally {
            if (servidor != null) {
                servidor.close();
            }
        }
    }
}

class Conexao extends Thread {
    Socket cliente;
    Scanner entrada;
    PrintStream saida;

    public Conexao(Socket paramCliente) {
        cliente = paramCliente;
        try {
            entrada = new Scanner(cliente.getInputStream());
            saida = new PrintStream(cliente.getOutputStream());
            this.start();
        } catch (IOException e) {
            System.err.println("Erro de conexao:" + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // Leitura do cabeçalho HTTP
            StringBuilder headerLines = new StringBuilder();
            while (entrada.hasNextLine()) {
                String linha = entrada.nextLine();
                headerLines.append(linha).append("\r\n");
                if (linha.isEmpty()) {
                    break; // Fim do cabeçalho HTTP
                }
            }

            // Enviando resposta HTTP com uma página HTML simples
            String respostaHTML = "<html><head><title>Servidor TCP</title></head><body><h1>Olá, mundo!</h1></body></html>";
            String respostaHTTP = "HTTP/1.1 200 OK\r\n" +
                                  "Content-Type: text/html\r\n" +
                                  "Content-Length: " + respostaHTML.length() + "\r\n" +
                                  "\r\n" +
                                  respostaHTML;
            saida.print(respostaHTTP);

            String ipCliente = cliente.getInetAddress().getHostAddress();
            int portaCliente = cliente.getPort();
            System.out.println("O cliente " + ipCliente + ", porta " + portaCliente + " desconectou.");
        } catch (Exception e) {
            System.err.println("Erro ao processar requisição HTTP: " + e.getMessage());
        } finally {
            try {
                cliente.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar socket cliente: " + e.getMessage());
            }
        }
    }
}

