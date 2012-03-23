package org.karatachi.portus.node.command;

import java.io.File;
import java.io.IOException;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.AssemblyInfo;
import org.karatachi.portus.node.PortusService;
import org.karatachi.portus.node.Service;
import org.karatachi.portus.node.http.PortusHttpService;
import org.karatachi.portus.node.monitor.PerformanceMonitor;

public class StatusCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        if (args.length != 1 && args.length != 2) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        int idx = 0;
        if (args.length == 2) {
            try {
                idx = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                return Command.INVALID_ARGUMENT;
            }
        }

        File dir = new File(AssemblyInfo.PATH_RAW_DATA);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        PortusService service = (PortusService) Service.getInstance();
        PortusHttpService http = service.getHttpService();
        PerformanceMonitor monitor = service.getPerformanceMonitor();

        sendStatus(200, "OK");

        Runtime runtime = Runtime.getRuntime();

        sendHeader("NodeId", AssemblyInfo.NODE_ID);
        sendHeader("TotalMemory", runtime.totalMemory());
        sendHeader("MaxMemory", runtime.maxMemory());
        sendHeader("FreeMemory", runtime.freeMemory());
        sendHeader("FreeSpace", dir.getUsableSpace());

        if (http != null) {
            sendHeader("MinThread", PortusHttpService.minThread);
            sendHeader("MaxThread", PortusHttpService.maxThread);
            sendHeader("Http-QueueCount", http.getQueueCount());
            sendHeader("Http-ActiveCount", http.getActiveCount());
            sendHeader("Http-TaskCount", http.getTaskCount());
            sendHeader("Http-CompletedTaskCount", http.getCompletedTaskCount());
        }

        for (String key : monitor.getKeys()) {
            double[] values = monitor.getValue(key);
            if (values != null && idx < values.length) {
                sendHeader(key, (long) values[idx]);
            } else {
                sendHeader(key, "null");
            }
        }

        return Command.OK;
    }

    @Override
    public String getCommand() {
        return "status";
    }
}
