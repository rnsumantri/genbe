package com.roni.genbe.controller;

import com.roni.genbe.model.dto.*;
import com.roni.genbe.model.entity.Biodata;
import com.roni.genbe.model.entity.Person;
import com.roni.genbe.repository.BiodataRepository;
import com.roni.genbe.repository.PendidikanRepository;
import com.roni.genbe.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/person")
public class PersonController {
    @Autowired
    private final PersonRepository personRepository;
    private final BiodataRepository biodataRepository;
    private final PendidikanRepository pendidikanRepository;

    @Autowired
    public PersonController(PersonRepository personRepository, BiodataRepository biodataRepository, PendidikanRepository pendidikanRepository){
        this.personRepository = personRepository;
        this.biodataRepository = biodataRepository;
        this.pendidikanRepository = pendidikanRepository;
    }

    @GetMapping
    public List<PersonDto> person(){
        List<Person> personList = personRepository.findAll();
        List<PersonDto> personDtoList = personList.stream().map(this::convertToDto)
                .collect(Collectors.toList());

        return personDtoList;
    }
    //Soal No 2
    @GetMapping("/pendidikan/{nik}")
    public List<Object> get (@PathVariable String nik){
        List<Object> status = new ArrayList<>();
        Response response = new Response();
        Response2 response2 = new Response2();

        if (nik.length() == 16) {
            if (personRepository.findByNik(nik)!=null && nik.length() == 16) {
                Response3 response3 = new Response3();
                PersonDto personDto = new PersonDto();
                Date birth = biodataRepository.findTglByPersonNik(nik);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(birth);
                response2.setStatus("true");
                response2.setMessage("success");
                response3.setName(personRepository.findNameByNik(nik));
                response3.setAddress(personRepository.findAddressByNik(nik));
                response3.setNik(personRepository.findByNik(nik));
                response3.setHp(biodataRepository.findNoHpByPersonNik(nik));
                response3.setDate(biodataRepository.findTglByPersonNik(nik));
                response3.setTempatLahir(biodataRepository.findTempatLahirByPersonNik(nik));
                String umur = String.valueOf(Year.now().getValue()-calendar.get(Calendar.YEAR)) ;
                response3.setUmur(umur);
                response3.setPendidikanTerakhir(pendidikanRepository.findJenjangByNik(nik));
                response2.setResponse3(response3);

                status.add(response2);
            } else {
                response.setStatus("false");
                response.setMessage("data dengan nik "+nik+" tidak ditemukan");
                status.add(response);
            }
        } else {
            response.setStatus("False");
            response.setMessage("Data gagal masuk, jumlah digit nik tidak sama dengan 16");
            status.add(response);
        }
        return status;
    }

    @PostMapping
    public Response insert (@RequestBody PersonDto personDto){
        Response response = new Response();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(personDto.getTgl());
        if (personDto.getNik().length()==16 && Year.now().getValue()-calendar.get(Calendar.YEAR)>=30){
            Person person = convertToEntityPerson(personDto);
            personRepository.save(person);
            Biodata biodata = convertToEntityBio(personDto, person.getIdPerson());
            biodataRepository.save(biodata);
            response.setStatus("true");
            response.setMessage("Data berhasil masuk");
        } else {
            response.setStatus("false");
            response.setMessage("Data gagal masuk, digit NIK tidak sama dengan 16 atau umur kurang dari 30 tahun");
        }
        return response;
    }
    private Biodata convertToEntityBio(PersonDto dto, Integer idPerson){
        Biodata biodata = new Biodata();
        biodata.setNoHp(dto.getHp());
        biodata.setTgl(dto.getTgl());
        biodata.setTempatLahir(dto.getTempatLahir());

        if (personRepository.findById(idPerson).isPresent()){
            Person person = personRepository.findById(idPerson).get();
            biodata.setPerson(person);
        }

        return biodata;
    }

    private Person convertToEntityPerson(PersonDto dto){
        Person person = new Person();
        person.setIdPerson(dto.getIdPerson());
        person.setNik(dto.getNik());
        person.setNama(dto.getName());
        person.setAlamat(dto.getAdress());

        return person;
    }


    private PersonDto convertToDto(Person person){
        PersonDto personDto = new PersonDto();
        personDto.setIdPerson(person.getIdPerson());
        personDto.setNik(person.getNik());
        personDto.setName(person.getNama());
        personDto.setAdress(person.getAlamat());

        return personDto;
    }




}
