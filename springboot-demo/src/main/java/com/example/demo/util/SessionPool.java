package com.example.demo.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.IntegrationPoint;
import com.mks.api.IntegrationPointFactory;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import org.springframework.stereotype.Component;

@Component
public class SessionPool {

    private static Log logger = LogFactory.getLog(SessionPool.class);

    private MksInfo mksinfo;

    private int count = 0;
    private int times = 0;

    private Queue<Session> freeConnection = new ArrayBlockingQueue<Session>(100);
    private static IntegrationPoint ip;

    public SessionPool(MksInfo mksinfo) {
        super();
        this.mksinfo = mksinfo;
        logger.info("********************************User: " + mksinfo.getUser());
        init();
        check();
    }

    private void init() {
        if (mksinfo != null) {
            for (int i = 0; i < mksinfo.getInitSession(); i++) {
                Session session = createSession();
                if (session != null) {
                    freeConnection.offer(session);
                }
                logger.info("********************************session: " + session);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private Session connect() throws APIException {
        if (ip == null) {
            ip = IntegrationPointFactory.getInstance().createIntegrationPoint(mksinfo.getHost(), mksinfo.getPort(),
                    false, mksinfo.getMajorVersion(), mksinfo.getMinorVersion());
            ip.setAutoStartIntegrityClient(true);
        }
        Session session = ip.createNamedSession(null, null, mksinfo.getUser(), mksinfo.getPassword());
        session.setDefaultUsername(mksinfo.getUser());
        session.setAutoReconnect(true);
        Command imConnect = new Command("im", "connect");
        CmdRunner cmdRunner = session.createCmdRunner();
        cmdRunner.setDefaultUsername(mksinfo.getUser());
        cmdRunner.setDefaultPassword(mksinfo.getPassword());
        cmdRunner.setDefaultHostname(mksinfo.getHost());
        cmdRunner.setDefaultPort(mksinfo.getPort());
        logger.warn(mksinfo.getHost());
        try {
            Response res = cmdRunner.execute(imConnect);
            logger.info("********************************Result: " + res.getExitCode());
        } catch (APIException e) {
            logger.info("********************************Session get Failure");
        } finally {
            if (cmdRunner != null) {
                cmdRunner.release();
            }
        }
        return session;
    }

    private Session createSession() {
        Session session = null;
        try {
            session = connect();
            count++;
        } catch (APIException e) {
            logger.warn(IntegrityUtil.getMessage(e));
            if (times++ < 3) {
                return createSession();
            }
        }
        return session;
    }

    public synchronized boolean sessionPeek() throws APIException {
        boolean sessionUseful = false;
        if (count < mksinfo.getMaxSessionSize() || !freeConnection.isEmpty()) {
            sessionUseful = true;
        }
        return sessionUseful;
    }

    public synchronized Session getSession() throws APIException {

        Session session = null;
        if (!freeConnection.isEmpty()) {
            session = freeConnection.poll();
            int i = 0;
            while (!sessionUsable(session)) {
                if (i > mksinfo.getWaitTimes()) {
                    freeConnection.offer(session);
                    throw new APIException("Session wait has been more than  " + i + " seconds.");
                }
                try {
                    wait(1000);
                    i++;
                } catch (InterruptedException e) {
                    logger.warn("CmdRunner wait InterruptedException: " + e);
                }
            }
            logger.info("freeConnection is not Empty, sessions: " + freeConnection.size() + ", session wait: " + i
                    + "seconds.");
            if (session == null && count < mksinfo.getMaxSessionSize()) {
                session = createSession();
            }
        } else {
            if (count < mksinfo.getMaxSessionSize()) {
                session = createSession();
            } else {
                logger.info("freeConnection is Empty. And connection count: " + count);
                int j = 0;
                while (freeConnection.isEmpty()) {
                    if (j > 30) {
                        throw new APIException("Session wait has been more than  " + j + " seconds.");
                    }
                    try {
                        wait(1000);
                        j++;
                    } catch (InterruptedException e) {
                        logger.warn("CmdRunner wait InterruptedException: " + e);
                    }
                }
                logger.info("freeConnection isEmpty: " + freeConnection.size() + ", session wait: " + j + "seconds.");
                session = freeConnection.poll();
            }
        }

        return session;
    }

    public synchronized void release(Session session) {
        freeConnection.offer(session);
    }

    public synchronized boolean sessionUsable(Session session) {
        boolean creatable = false;
        if (session == null) {
            return creatable;
        }
        Iterator<?> it = session.getCmdRunners();
        int i = 0;
        while (it.hasNext()) {
            it.next();
            i++;
        }
        logger.info("Users: " + session.getDefaultUsername() + ", session cmdRunners: " + i);
        if (i < mksinfo.getMaxCmdRunners()) {
            creatable = true;
        }
        return creatable;
    }

    public synchronized void destory() {
        while (!freeConnection.isEmpty()) {
            try {
                Session session = freeConnection.poll();
                session.release();
                count--;
            } catch (IOException e) {
                logger.error("destory: " + e.getMessage());
            } catch (APIException e) {
                logger.error("destory: " + e.getMessage());
            }
        }
    }

    public void check() {
        new Timer().schedule(new TimerTask() {

            @Override
            public synchronized void run() {
                while (freeConnection.size() > mksinfo.getInitSession()) {
                    try {
                        Session session = freeConnection.poll();
                        session.release();
                        count--;
                    } catch (IOException e) {
                        logger.error("check: " + e.getMessage());
                    } catch (APIException e) {
                        logger.error("check: " + e.getMessage());
                    }
                }
            }
        }, mksinfo.getLazyCheck() * 1000, mksinfo.getPeriodCheck() * 1000);
    }

}
