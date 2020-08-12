package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Implements the car service create, read, update or delete information about vehicles, as well as
 * gather related location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final PriceClient priceWebClient;
    private final MapsClient mapsWebClient;

    public CarService(CarRepository repository, PriceClient priceWebClient,
            MapsClient mapsWebClient) {
        this.mapsWebClient = mapsWebClient;
        this.priceWebClient = priceWebClient;
        this.repository = repository;
    }

    /**
     * Gathers a list of all vehicles
     *
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     *
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Optional<Car> car = repository.findById(id);
        Car carToReturn;
        if (car.isPresent()) {
            carToReturn = car.get();
            String price = priceWebClient.getPrice(id);
            carToReturn.setPrice(price);
            Location location = mapsWebClient.getAddress(carToReturn.getLocation());
            carToReturn.setLocation(location);
        } else {
            throw new CarNotFoundException();
        }
        return carToReturn;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     *
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     *
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> car = repository.findById(id);
        if(car.isPresent()) {
            Car carToDelete = car.get();
            repository.delete(carToDelete);
        } else {
            throw new CarNotFoundException();
        }
    }
}
