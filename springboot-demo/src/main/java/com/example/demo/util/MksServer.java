package com.example.demo.util;


import com.mks.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * MKS服务
 */
@Deprecated
@Component
public class MksServer {

    private static final Logger log = LoggerFactory.getLogger(MksServer.class);

    @Value(value = "${MKS.host}")
    private String host;
    @Value("${MKS.port}")
    private int port;
    @Value("${MKS.user}")
    private String user;
    @Value("${MKS.password}")
    private String password;

    public MksServer() {

    }

    /**
     * 获取CmdRunner。
     *
     * @return CmdRunner
     */
    public CmdRunner getCmdRunner() {
        try {
            log.warn(String.format("host=%s,port=%s,user=%s,password=%s", host, port, user, password));
            IntegrationPointFactory mksIpf = IntegrationPointFactory.getInstance();
            IntegrationPoint mksIp = mksIpf.createLocalIntegrationPoint(4, 16);
            mksIp.setAutoStartIntegrityClient(true);
            Session mksSession = mksIp.getCommonSession();
            CmdRunner mksCmdRunner = mksSession.createCmdRunner();
            mksCmdRunner.setDefaultUsername(user);
            mksCmdRunner.setDefaultPassword(password);
            mksCmdRunner.setDefaultHostname(host);
            mksCmdRunner.setDefaultPort(port);
            return mksCmdRunner;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 生成CmdRunner。
     *
     * @return CmdRunner
     */
    public CmdRunner createCmdRunner() {
        try {
            log.warn(String.format("host=%s,port=%s,user=%s,password=%s", host, port, user, password));
            IntegrationPointFactory mksIpf = IntegrationPointFactory.getInstance();
            IntegrationPoint mksIp = mksIpf.createIntegrationPoint(host, port, 4, 16);
            Session commonSession = mksIp.createSession(user, password);
            CmdRunner mksCmdRunner = commonSession.createCmdRunner();
            mksCmdRunner.setDefaultHostname(host);
            mksCmdRunner.setDefaultPort(port);
            mksCmdRunner.setDefaultUsername(user);
            mksCmdRunner.setDefaultPassword(password);
            return mksCmdRunner;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public void closeCmdRunner(CmdRunner cmdRunner) {
        try {
            Command cmd = new Command("aa", "disconnect");
            cmd.addOption(new Option("hostname", host));
            cmd.addOption(new Option("port", String.valueOf(port)));
            cmd.addOption(new Option("user", user));
            cmdRunner.execute(cmd);
            log.info("断开链接{}:{}", host, port);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
