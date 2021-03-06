package br.com.caelum.restfulie.vraptor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.rest.Restfulie;
import br.com.caelum.vraptor.rest.StateResource;
import br.com.caelum.vraptor.rest.Transition;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;

public class LinkConverterTest {
	
	public static class Client implements StateResource{
		
		private transient final Transition[] list;

		public Client(Transition ... t) {
			this.list = t;
		}

		public List<Transition> getFollowingTransitions(Restfulie control) {
			return Arrays.asList(list);
		}
		
	}
	private XStream xstream;
	
	@Before
	public void setup() {
		this.xstream = new XStream();
		Configuration config = mock(Configuration.class);
		when(config.getApplicationPath()).thenReturn("http://www.caelum.com.br");
		xstream.registerConverter(new LinkConverter(new ReflectionConverter(xstream.getMapper(), xstream.getReflectionProvider()), null, config));
	}
	
	@Test
	public void shouldSerializeNoLinksIfThereIsNoTransition() {
		String xml = xstream.toXML(new Client());
		assertThat(xml, not(containsString("atom:link")));
	}
	
	@Test
	public void shouldSerializeOneLinkIfThereIsATransition() {
		Transition t = mock(Transition.class);
		when(t.getName()).thenReturn("kill");
		when(t.getUri()).thenReturn("/kill");
		
		String xml = xstream.toXML(new Client(t));
		assertThat(xml, containsString("<atom:link rel=\"kill\" href=\"http://www.caelum.com.br/kill\" xmlns:atom=\"http://www.w3.org/2005/Atom\"/>"));
	}
	
	@Test
	public void shouldSerializeAllLinksIfThereAreTransitions() {
		Transition t = mock(Transition.class);
		when(t.getName()).thenReturn("kill");
		when(t.getUri()).thenReturn("/kill");
		
		Transition t2 = mock(Transition.class);
		when(t2.getName()).thenReturn("ressurect");
		when(t2.getUri()).thenReturn("/ressurect");
		
		String xml = xstream.toXML(new Client(t,t2));
		assertThat(xml, containsString("<atom:link rel=\"kill\" href=\"http://www.caelum.com.br/kill\" xmlns:atom=\"http://www.w3.org/2005/Atom\"/>"));
		assertThat(xml, containsString("<atom:link rel=\"ressurect\" href=\"http://www.caelum.com.br/ressurect\" xmlns:atom=\"http://www.w3.org/2005/Atom\"/>"));
	}
	
}
