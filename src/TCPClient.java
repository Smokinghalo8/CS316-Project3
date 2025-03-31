import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.rmi.ConnectException;
import java.util.Scanner;


public class TCPClient {
    public static void main(String[] args) throws Exception {
        ClientErrorHandling errorHandling = new ClientErrorHandling();

        int serverPort = Integer.parseInt(args[1]);
        Scanner keyboard = new Scanner(System.in);
        String commandName;

        errorHandling.CheckForCorrectAmountOfArguments(args);
        try {
            while(true){
                do {
                    System.out.println("Please Enter A Command:");
                    System.out.println("List. List of File \nDelete. Delete a File \nRename. Rename a File \nDownload. Download a File \nUpload. Upload a File");
                    commandName = keyboard.nextLine();
                    commandName = commandName.toUpperCase();
                } while (!errorHandling.CheckForProperCommand(commandName));

                SocketChannel channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(args[0],serverPort));
                Thread.sleep(10000);
                ByteBuffer buffer = ByteBuffer.wrap(commandName.getBytes());
                channel.write(buffer);

                switch (commandName){
                    case "LIST":
                        serverOutput(channel);
                        break;
                    case "DELETE":
                        deleteFile(channel);
                        break;
                    case "RENAME":
                        renameFile(channel);
                        break;
                    case "DOWNLOAD":
                        downloadFile(channel);
                        break;
                    case "UPLOAD":
                        uploadFile(channel);
                        break;
                }
                channel.close();
            }
        }catch (ConnectException e){
            System.out.println("Connection has been terminated");
        }

   }

   static void deleteFile(SocketChannel channel) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter the name of the file you would like to delete: ");
        String fileName = input.nextLine();
        ByteBuffer buffer = ByteBuffer.wrap(fileName.getBytes());
        channel.write(buffer);
       channel.shutdownOutput();
    }

   static void renameFile(SocketChannel channel) throws IOException {
        Scanner input = new Scanner(System.in);
        StringBuilder filenames = new StringBuilder();
        System.out.println("Enter the name of the file you would like to rename (including extension)");
        filenames.append(input.nextLine()).append("\n");

        System.out.println("Enter the name you would like to name the file");
        filenames.append(input.nextLine());
        ByteBuffer buffer = ByteBuffer.wrap(filenames.toString().getBytes());
        channel.write(buffer);
        serverOutput(channel);
    }

   static void downloadFile(SocketChannel channel) throws IOException {
       Scanner input = new Scanner(System.in);
       System.out.println("Enter the file you would like to download");
       String file = input.nextLine();
       ByteBuffer buffer = ByteBuffer.wrap(file.getBytes());
       channel.write(buffer);

       FileOutputStream fs = new FileOutputStream("ClientFiles/"+file,true);
       FileChannel fc = fs.getChannel();
       ByteBuffer fileContent = ByteBuffer.allocate(1024);
       while(channel.read(fileContent)>=0){
           fileContent.flip();
           fc.write(fileContent);
           fileContent.clear();
       }
       channel.shutdownOutput();
   }

   static void uploadFile(SocketChannel channel) throws IOException {
       Scanner input = new Scanner(System.in);
       System.out.println("Enter the file you would like to upload");
       String file = input.nextLine();
       ByteBuffer buffer = ByteBuffer.wrap(file.getBytes());
       channel.write(buffer);

       FileInputStream fs = new FileInputStream("ClientFiles/"+file);
       FileChannel fc = fs.getChannel();
       ByteBuffer fileContent = ByteBuffer.allocate(1024);
       int byteRead;
       do {
           byteRead = fc.read(fileContent);
           fileContent.flip();
           channel.write(fileContent);
           fileContent.clear();
       }while(byteRead>=0);
       channel.shutdownOutput();
       fs.close();
       serverOutput(channel);
   }

   static void serverOutput(SocketChannel channel) throws IOException {
       ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
       int bytesRead = channel.read(replyBuffer);
       replyBuffer.flip();
       byte[] a = new byte[bytesRead];
       replyBuffer.get(a);
       System.out.println(new String(a));
   }
}
