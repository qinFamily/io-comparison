package being.altiplano.ioservice;

import being.altiplano.ioservice.bio.BioClient;
import being.altiplano.ioservice.bio.BioServer;
import being.altiplano.ioservice.junitext.PrintEntrance;
import being.altiplano.ioservice.nio.NioClient;
import being.altiplano.ioservice.nio.NioServer;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gaoyuan on 22/02/2017.
 */
public class ServerClientTest extends ServerClientTestBase {

    @Rule
    public PrintEntrance printEntrance = new PrintEntrance();

    protected void doTest_SC(final Class<? extends IServer> serverClz,
                             final Class<? extends IClient> clientClz) throws IOException {
        boolean success = false;
        try (IServer server = createSocketServer(serverClz)) {
            server.start();
            IClient client = createSocketClient(clientClz);

            checkEcho(client);
            checkCount(client);
            checkReverse(client);
            checkUpperCast(client);
            checkLowerCast(client);

            checkRandom(client, 5 + random.nextInt(10));

            closeSocketClient(client);
            success = true;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            collector.addError(e);
        } finally {
            collector.checkThat(success, CoreMatchers.is(true));
        }
    }

    public void doTest_SnC(final Class<? extends IServer> serverClz,
                           final Class<? extends IClient> clientClz) throws IOException {
        int clientCount = 1 + random.nextInt(10);
        doTest_Server_N_Client(serverClz, clientClz, clientCount);
    }

    public void doTest_Server_N_Client(final Class<? extends IServer> serverClz,
                                       final Class<? extends IClient> clientClz, final int clientCount) throws IOException {
        boolean success = false;
        final AtomicInteger successCounter = new AtomicInteger(0);
        final CountDownLatch clientLatch = new CountDownLatch(clientCount);
        final CyclicBarrier clientBarrier = new CyclicBarrier(clientCount);

        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
        try (IServer server = createSocketServer(serverClz)) {
            server.start();

            for (int c = 0; c < clientCount; ++c) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IClient client = createSocketClient(clientClz);
                            clientBarrier.await();

                            checkEcho(client);
                            checkCount(client);
                            checkReverse(client);
                            checkUpperCast(client);
                            checkLowerCast(client);

                            checkRandom(client, 5 + new Random().nextInt(10));

                            closeSocketClient(client);
                            successCounter.incrementAndGet();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            collector.addError(e);
                        } finally {
                            clientLatch.countDown();
                        }
                    }
                };

                es.submit(runnable);
            }
            clientLatch.await();
            success = true;
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            collector.addError(e);
        } finally {
            collector.checkThat("" + serverClz + "," + clientClz, clientCount, CoreMatchers.equalTo(successCounter.get()));
            collector.checkThat("" + serverClz + "," + clientClz, success, CoreMatchers.is(true));
        }
    }

    protected final Class<? extends IServer>[] serverTypes = new Class[]{BioServer.class, NioServer.class};
    protected final Class<? extends IClient>[] clientTypes = new Class[]{BioClient.class};

//    @Test(timeout = 10_000)
//    public void test_BioServer_BioClient() throws IOException {
//        final Class<? extends IServer> serverClz = BioServer.class;
//        final Class<? extends IClient> clientClz = BioClient.class;
//        doTest_SC(serverClz, clientClz);
//    }

    @Test(timeout = 1000_000)
    public void test_BioServer_NioClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = NioClient.class;
        doTest_SC(serverClz, clientClz);
    }

//    @Test(timeout = 10_000)
//    public void test_NioServer_BioClient() throws IOException {
//        final Class<? extends IServer> serverClz = NioServer.class;
//        final Class<? extends IClient> clientClz = BioClient.class;
//        doTest_SC(serverClz, clientClz);
//    }

//    @Test
//    public void test_XServer_XClient() throws IOException {
//        for (Class<? extends IServer> serverClz : serverTypes){
//            for (Class<? extends IClient> clientClz : clientTypes ){
//                doTest_SC(serverClz, clientClz);
//            }
//        }
//    }
//
//    @Test
//    public void test_XServer_XClient_N() throws IOException {
//        for (Class<? extends IServer> serverClz : serverTypes){
//            for (Class<? extends IClient> clientClz : clientTypes ){
//                doTest_SnC(serverClz, clientClz);
//            }
//        }
//    }
}