package de.rememberbrall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class RememberbrallControllerTest {

    @InjectMocks
    private RememberbrallController rememberbrallController;
    @Mock
    private RememberbrallService rememberbrallService;
    @Mock
    private Entry entry;


    private static final String ID_EXAMPLE = "00000000-0000-0000-0000-000000000002";

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void showAllEntries() {
        //given
        when(rememberbrallService.getAllEntries()).thenReturn(Flux.just(entry, entry, entry));

        //when
        Flux<Entry> allEntries = rememberbrallController.showAllEntries();

        //then
        StepVerifier.create(allEntries)
                .expectNext(entry, entry, entry)
                .verifyComplete();
    }

    @Test
    public void showSpecificExistingEntry() {
        //given
        when(rememberbrallService.getEntryByID(ID_EXAMPLE)).thenReturn(Mono.just(entry));

        //when
        ResponseEntity<Entry> specificEntry = rememberbrallController.showSpecificEntry(ID_EXAMPLE);

        //then
        assertThat(specificEntry.getBody()).isEqualTo(entry);
    }

    @Test
    public void showSpecificNonExistingEntry() {
        //given
        when(rememberbrallService.getEntryByID(ID_EXAMPLE)).thenReturn(Mono.empty());

        //when
        ResponseEntity<Entry> specificEntry = rememberbrallController.showSpecificEntry(ID_EXAMPLE);

        //then
        assertThat(specificEntry.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void createEntry() {
        //given
        Mono<Entry> entryAsMono = Mono.just(entry);
        when(rememberbrallService.createEntry(entry)).thenReturn(entryAsMono);
        when(entryAsMono.block().getId()).thenReturn(ID_EXAMPLE);

        //when
        ResponseEntity<Entry> newEntry = rememberbrallController.createEntry(entry);

        //then
        assertThat(newEntry.getStatusCode()).isSameAs(HttpStatus.CREATED);
        assertThat(newEntry.getHeaders().getLocation().toString()).isEqualTo(ID_EXAMPLE);
    }

    @Test
    public void deleteEntry() {
        //given
        when(rememberbrallService.deleteEntry(ID_EXAMPLE)).thenReturn(Mono.empty());
        //when
        ResponseEntity<?> deleteEntry = rememberbrallController.deleteEntry(ID_EXAMPLE);
        //then
        assertThat(deleteEntry.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void deleteAllEntries() {
        //given
        when(rememberbrallService.deleteAllEntries()).thenReturn(Mono.empty());
        //when
        ResponseEntity<?> deleteAllEntries = rememberbrallController.deleteAllEntries();
        //then
        assertThat(deleteAllEntries.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void updateEntry() throws MalformedURLException {
        //given
        when(rememberbrallService.updateEntry(ID_EXAMPLE, new Entry("New Entry Name", EntryCategory.LINUX, new URL("http://www.new-url.de")))).thenReturn(Mono.just(entry));

        //when
        ResponseEntity<Entry> updatedEntry = rememberbrallController.updateEntry(ID_EXAMPLE, new Entry("New Entry Name", EntryCategory.LINUX, new URL("http://www.new-url.de")));

        //then
        assertThat(updatedEntry.getStatusCode()).isSameAs(HttpStatus.OK);
    }

    @Test
    public void updateNonExistingEntry() throws MalformedURLException {
        //given
        when(rememberbrallService.updateEntry(ID_EXAMPLE, new Entry("New Entry Name", EntryCategory.LINUX, new URL("http://www.new-url.de")))).thenReturn(Mono.empty());

        //when
        ResponseEntity<Entry> updatedEntry = rememberbrallController.updateEntry(ID_EXAMPLE, new Entry("New Entry Name", EntryCategory.LINUX, new URL("http://www.new-url.de")));

        //then
        assertThat(updatedEntry.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
