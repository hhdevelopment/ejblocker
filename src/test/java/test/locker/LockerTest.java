/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.locker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.ejb.EJB;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(Arquillian.class)
public class LockerTest {

	@Inject
	private Logger logger;
	@EJB
	private AsyncBean asyncBean;
	/**
	 * Active/desactive le test de temps
	 */
	private boolean testDelay = true;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		File[] libs = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();
		EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
				  .addAsLibraries(libs)
				  .addAsModule(createLockerModuleArchive())
				  .addAsModule(createTestArchive());
		System.out.println(ear.toString(true));
		return ear;
	}

	/**
	 * logger est ajouté à l'ear en tant que librairie
	 *
	 * @return
	 */
	public static JavaArchive createLockerModuleArchive() {
		File beans = new File("src/main/resources/META-INF/beans.xml");
		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "locker.jar")
				  .addAsManifestResource(new FileAsset(beans), ArchivePaths.create("beans.xml"))
				  .addPackages(true, "hhf.locker");
		System.out.println(jar.toString(true));
		return jar;
	}

	/**
	 * Les classes de tests sont ajoutées à l'ear comme module ejb, car la classe doit être managé
	 *
	 * @return
	 */
	public static JavaArchive createTestArchive() {
		File logback = new File("src/test/resources/logback-test.xml");
		File beans = new File("src/main/resources/META-INF/beans.xml");
		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "test.jar")
				  .addAsResource(new FileAsset(logback), ArchivePaths.create("logback-test.xml"))
				  .addAsManifestResource(new FileAsset(beans), ArchivePaths.create("beans.xml"))
				  .addClasses(LockerTest.class, LockedBean.class, AsyncBean.class);
		System.out.println(jar.toString(true));
		return jar;
	}

	/**
	 * Test d'une methode annoté au niveau de la methode mais aussi de l'argument la clé de verrou est donc constituée du nom de la méthode et de la valeur de l'argument la methode incrémente une
	 * valeur liée à  l'argument. La valeur est lu, on attend, on incremente la valeur, puis on la sauvegarde ainsi, si le verrou se passe correctement, la valeur devait successivement passer de 1, 2,
	 * 3... etc l'attente entre increment est de 200ms, donc l'execution de ce test devrait prendre plus de 2 secondes
	 *
	 * @throws Exception
	 */
	@Test
	public void testLockedMethodWithAnnotedArgument() throws Exception {
		List<Future<Integer>> futuresa = new ArrayList<Future<Integer>>();
		List<Future<Integer>> futuresb = new ArrayList<Future<Integer>>();
		List<Future<Integer>> futuresc = new ArrayList<Future<Integer>>();
		long t0 = System.currentTimeMillis();
		int nbtest = 10;
		Integer attempt = 0;
		for (int i = 0; i < nbtest; i++) {
			attempt += i; // 0+1+2+3+4+5+6+7+8+9 = 45
			futuresa.add(asyncBean.accessLockedMethodWithAnnotedArgument("a"));
			futuresb.add(asyncBean.accessLockedMethodWithAnnotedArgument("b"));
			futuresc.add(asyncBean.accessLockedMethodWithAnnotedArgument("c"));
		}
		Integer resulta = 0;
		Integer resultb = 0;
		Integer resultc = 0;
		for (int i = 0; i < nbtest; i++) {
			resulta += futuresa.get(i).get();
			resultb += futuresb.get(i).get();
			resultc += futuresc.get(i).get();
		}
		long time = System.currentTimeMillis() - t0;
		int s = (int) time / 1000;
		logger.info("Test a : attempt {} : was {}", attempt, resulta);
		logger.info("Test b : attempt {} : was {}", attempt, resultb);
		logger.info("Test c : attempt {} : was {}", attempt, resultc);
		assertEquals(attempt, resulta);
		assertEquals(attempt, resultb);
		assertEquals(attempt, resultc);
		logger.info("Temps d'excution = {}ms", time);
		if (testDelay) {
			assertEquals((nbtest * 200) / 1000, s);
		}
	}

	/**
	 * Test d'une methode non annoté le résultat doit être erronné le test etant multithread il doit être bref.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUnlockedMethodWithAnnotedArgument() throws Exception {
		List<Future<Integer>> futuresa = new ArrayList<Future<Integer>>();
		List<Future<Integer>> futuresb = new ArrayList<Future<Integer>>();
		List<Future<Integer>> futuresc = new ArrayList<Future<Integer>>();
		long t0 = System.currentTimeMillis();
		int nbtest = 10;
		Integer attempt = 0;
		for (int i = 0; i < nbtest; i++) {
			attempt += i; // 0+1+2+3+4+5+6+7+8+9 = 45
			futuresa.add(asyncBean.accessUnlockedMethodWithAnnotedArgument("d"));
			futuresb.add(asyncBean.accessUnlockedMethodWithAnnotedArgument("e"));
			futuresc.add(asyncBean.accessUnlockedMethodWithAnnotedArgument("f"));
		}
		Integer resulta = 0;
		Integer resultb = 0;
		Integer resultc = 0;
		for (int i = 0; i < nbtest; i++) {
			resulta += futuresa.get(i).get();
			resultb += futuresb.get(i).get();
			resultc += futuresc.get(i).get();
		}
		long time = System.currentTimeMillis() - t0;
		int s = (int) time / 1000;
		logger.info("Test d : non attempt {} : was {}", attempt, resulta);
		logger.info("Test e : non attempt {} : was {}", attempt, resultb);
		logger.info("Test f : non attempt {} : was {}", attempt, resultc);
		assertTrue(attempt != resulta);
		assertTrue(attempt != resultb);
		assertTrue(attempt != resultc);
		logger.info("Temps d'excution = {}ms", time);
		if (testDelay) {
			assertEquals(0, s);
		}
	}

	/**
	 * Dans ce test deux methodes utilisent une clé commune Donc elles se bloquent l'une l'autre
	 *
	 * @throws Exception
	 */
	@Test
	public void testLockedMethodWithAnnotation() throws Exception {
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		long t0 = System.currentTimeMillis();
		int nbtest = 10;
		Integer attempt = 0;
		for (int i = 0; i < nbtest; i++) {
			attempt += i; // 0+1+2+3+4+5+6+7+8+9 = 45
			futures.add(asyncBean.accessLockedMethod1WithAnnotation("g"));
			attempt += nbtest + i; // 10+11+12+13+14+15+16+17+18+19 = 145
			futures.add(asyncBean.accessLockedMethod2WithAnnotation("g"));
		}
		Integer result = 0;
		for (int i = 0; i < nbtest * 2; i++) {
			result = result + futures.get(i).get();
		}
		long time = System.currentTimeMillis() - t0;
		int s = (int) time / 1000;
		logger.info("Test : attempt {} : was {}", attempt, result);
		assertEquals(attempt, result);
		logger.info("Temps d'excution = {}ms", time);
		if (testDelay) {
			assertEquals((nbtest * 2 * 200) / 1000, s);
		}
	}
}
