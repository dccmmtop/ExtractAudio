package top.dc;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Strings;

import java.io.*;

/**
 * Hello world!
 */
public class App {

    @Parameter(names = "-url", description = "bilibili链接，或者YouTube链接")
    private String url;

    @Parameter(names = "-h", required = false, description = "帮助文档")
    private boolean help;

    public static void main(String[] args) throws IOException {

        App app = new App();
        // 将jar包中的exe文件拷贝与jar同级目录
        app.copyExe();
        JCommander jc = JCommander.newBuilder()
                .addObject(app)
                .build();
        jc.parse(args);
        if (app.help) {
            jc.usage();
            return;
        }
        app.run();
    }

    private void copyExe() throws IOException {
        String ytPath = new File("").getAbsoluteFile() + File.separator + "yt-dlp.exe";
        if(new File(ytPath).exists()){
            System.out.println("yt-dlp 存在，跳过");
            return;
        }
        System.out.println("复制 yt-dlp");
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("yt-dlp.exe");
        OutputStream os = new FileOutputStream(new File("./yt-dlp.exe"));
        int len = 1024;
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buf)) > 0) {
            os.write(buf, 0, bytesRead);
        }
        os.close();
        in.close();
        System.out.println("复制完毕!");
    }

    private void run() {
        String ytPath = new File("").getAbsoluteFile() + File.separator + "yt-dlp.exe";
        System.out.println(ytPath);
        String[] cmd = {"cmd", "/c", ytPath, "-f", "\"ba\"", "-x",
                "--audio-format", "mp3", "\"" + url + "\"",
                "-o", "tmp_%(title)s"};
        System.out.println("执行的命令是: \n" + Strings.join(" ", cmd));
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            try {
                process.waitFor();

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                line = "";
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("下载完毕!");

                renameLastedAudio();
                String[] deleteTmpFileCmd = {"cmd","/c","rm *.part"};
                Runtime.getRuntime().exec(deleteTmpFileCmd);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void renameLastedAudio() throws IOException {
        String path = "./";
        File file = new File(path);
        File[] fs = file.listFiles();
        int maxNum = -1;
        String lastedAudio = "";
        assert fs != null;
        for (File f : fs) {
            if (!f.isDirectory()) {
                String currentNum = f.getName().split("_")[0];
                if (currentNum.matches("^\\d+?")) {
                    maxNum = Math.max(Integer.parseInt(currentNum), maxNum);
                }
                if (f.getName().startsWith("tmp_")) {
                    lastedAudio = f.getAbsolutePath();
                }
            }
        }

        String newName = lastedAudio.replace("tmp_", String.valueOf(maxNum + 1) + "_");
        System.out.println("最新文件名是: " + newName);
        rename(lastedAudio, newName);
        System.out.println("结束");
    }

    private void rename(String oldName, String newName) throws IOException {
        // 旧的文件或目录
        File oldNameFile = new File(oldName);
        // 新的文件或目录
        File newNameFile = new File(newName);
        if (newNameFile.exists()) {  //  确保新的文件名不存在
            throw new java.io.IOException(newName + "已经存在s");
        }
        if (oldNameFile.renameTo(newNameFile)) {
            System.out.println("已重命名");
        } else {
            System.out.println("Error");
        }
    }
}
