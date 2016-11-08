package com.sopra.rest.buisness;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.sopra.rest.FunkoPop;
import com.sopra.rest.IGoogleMapsDirectionsWS;
import com.sopra.rest.IWeatherWs;
import com.sopra.rest.directions.DirectionsResult;

@Stateless
public class FunkoPopService
{
	@PersistenceContext
	EntityManager em;
	
	//private List<FunkoPop> pops = new ArrayList<>();
	//private int nextId = 1;

	@PostConstruct 
	public void sayHelloIAmAlive(){
		System.out.println("I am a FunkoPop service ans I am alive");
	}
	
	@PreDestroy
	public void	destroying(){
		System.out.println("Arrgggghhhhhh !!!!!!");
	}
	
	/*
	public FunkoPopService()
	{
		pops.add( new FunkoPop( nextId++, "Gandalf", "Lord of the Ring", true, 43.635191, 1.481871 ) );
		pops.add( new FunkoPop( nextId++, "Alf", "Alf", false, 43.641092, 1.447453 ) );
		pops.add( new FunkoPop( nextId++, "Joey Tempest", "Europe", false, 43.607603, 1.403164 ) );
		pops.add( new FunkoPop( nextId++, "ZombiGirl", "Walking Dead", true, 43.551469, 1.244615 ) );
		System.out.println("End of constructor");
	}*/
	
	public List<FunkoPop> findAll()
	{
		//return pops;
		//this is not SQL, it is JPQL: Java Persistence Query Language
		return em.createQuery("SELECT f FROM FunkoPop f", FunkoPop.class).getResultList();
	}

	public void delete( int id )
	{
		FunkoPop pop = findFunkoPopById( id );
		if( pop != null )
			em.remove( pop ); //remplacage de pop par em
	}

	public FunkoPop createOrUpdate( FunkoPop pop )
	{
		FunkoPop existing = findFunkoPopById( pop.getId() );
		if( existing != null ) //update
		{
			existing.setName( pop.getName() );
			existing.setUniverse( pop.getUniverse() );
			existing.setWaterproof( pop.isWaterproof() );
			existing.setLongitude( pop.getLongitude() );
			existing.setLatitude( pop.getLatitude() );

			return existing;
		}
		else //create
		{
			pop.setId( 0 );
			em.persist( pop );
			return pop;
		}
	}

	public List<FunkoPop> search( String name, String universe )
	{
		List<FunkoPop> result = new ArrayList<>();
		/*for( FunkoPop pop : pops )
		{
			if( isValid( pop.getName(), name ) && isValid( pop.getUniverse(), universe ) )
				result.add( pop );
		}*/
		return result;
	}

	public List<FunkoPop> getFunkoPopsToShelter()
	{
		// savoir s'il fait beau ou pas
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target( "http://localhost:8080/WeatherWS/weather/isWeatherGood" );
		IWeatherWs simple = target.proxy( IWeatherWs.class );
		boolean weatherGood = simple.isWeatherGood();

		List<FunkoPop> result = new ArrayList<>();

		if( !weatherGood )
		{
			/*for( FunkoPop pop : pops )
			{
				if( !pop.isWaterproof() )
					result.add( pop );
			}*/
		}

		return result;
	}

	public int getTravelTime( int fromFunkoPopId, int toFunkoPopId )
	{
		FunkoPop from = findFunkoPopById( fromFunkoPopId );
		if( from == null )
			return -1;

		FunkoPop to = findFunkoPopById( toFunkoPopId );
		if( to == null )
			return -1;

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target( "https://maps.googleapis.com/maps/api" );
		IGoogleMapsDirectionsWS directions = target.proxy( IGoogleMapsDirectionsWS.class );

		String origin = from.getLatitude() + ", " + from.getLongitude();
		String destination = to.getLatitude() + ", " + to.getLongitude();

		DirectionsResult result = directions.getTravelTime(
				origin, destination, "AIzaSyDJluJ1olY-2KLGOqU9YDXs67wsCmIZkng" );

		return (int) result.routes[0].legs[0].duration.value;
	}

	public FunkoPop findFunkoPopById( int funkoPopId )
	{
		/*for( FunkoPop pop : pops )
			if( pop.getId() == funkoPopId )
				return pop;*/

		return em.find(FunkoPop.class, funkoPopId);
	}

	private boolean isValid( String value, String criteria )
	{
		return criteria == null || (value != null && value.contains( criteria ));
	}
}
