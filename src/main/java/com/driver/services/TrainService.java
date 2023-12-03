package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        // create new train object
        Train train = new Train();

        // get station route list from dto and set to train obj in the form of string
        StringBuilder sb = new StringBuilder();
        for(Station s : trainEntryDto.getStationRoute()){
            sb.append(s.toString());
            sb.append(',');
        }
        sb.deleteCharAt(sb.length()-1); // to remove last comma

        // set all properties to train obj
        train.setRoute(sb.toString());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        // save train in database
        Train newTrain = trainRepository.save(train);

        return newTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        // get the train
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        int availSeats = train.getNoOfSeats();
        String route = train.getRoute();

        int start = route.indexOf(seatAvailabilityEntryDto.getFromStation().toString());
        int end = route.indexOf(seatAvailabilityEntryDto.getToStation().toString());

        // get all booked tickets on this train and deduct the seats count according to our journey
        for(Ticket ticket : train.getBookedTickets()){
            int bookSrc = route.indexOf(ticket.getFromStation().toString());
            int bookDest = route.indexOf(ticket.getToStation().toString());
            // now check current ticket shows any effect on seats availability in our journey
            if(bookSrc < end && bookDest > start){
                availSeats -= ticket.getPassengersList().size();
            }
        }

       return availSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        // get train
        Train train = trainRepository.findById(trainId).get();
        String route = train.getRoute();

        // if train not passing through this station throw exception
        if(route.indexOf(station.toString()) == -1){
            throw new Exception("Train is not passing from this station");
        }

        // otherwise do our calculation
        int passengersCount = 0;

        for(Ticket ticket : train.getBookedTickets()){
            if(ticket.getFromStation().toString().equals(station.toString())){
                passengersCount += ticket.getPassengersList().size();
            }
        }

        return passengersCount;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        int oldestPersonAge = 0;

        Train train = trainRepository.findById(trainId).get();

        for(Ticket ticket : train.getBookedTickets()){
            // get all passengers booked on under this ticket
            for(Passenger passenger : ticket.getPassengersList()){
                if(passenger.getAge() > oldestPersonAge){
                    oldestPersonAge = passenger.getAge();
                }
            }
        }

        return oldestPersonAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        int start = startTime.getHour() * 60 + startTime.getMinute();
        int end = endTime.getHour() * 60 + endTime.getMinute();

        List<Integer> trainsList = new ArrayList<>();

        // get all trains and check every train that passing through this station in given duration
        for(Train train : trainRepository.findAll()){
            // check that train passing through this station
            String route = train.getRoute();
            String[] stations = route.split(",");

            for(int i=0; i<stations.length; i++){
                String currSt = stations[i];
                if(currSt.equals(station.toString())){
                    int timeToReachStation = ((train.getDepartureTime().getHour() + i) * 60) + (train.getDepartureTime().getMinute());

                    if(timeToReachStation >= start && timeToReachStation <= end){
                        trainsList.add(train.getTrainId());
                    }
                    // break loop
                    break;
                }
            }
        }

        return trainsList;
    }

}
