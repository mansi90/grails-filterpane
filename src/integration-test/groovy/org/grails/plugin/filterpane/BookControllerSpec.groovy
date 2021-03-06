package org.grails.plugin.filterpane

import grails.core.GrailsApplication
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.util.Holders
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@TestFor(BookController)
class BookControllerSpec extends Specification {
    @Autowired
    FilterPaneService filterPaneService
    @Autowired
    GrailsApplication grailsApplication

    def setup() {
        filterPaneService = new FilterPaneService()
        filterPaneService.grailsApplication = grailsApplication
        controller.filterPaneService = filterPaneService
    }

    def "test emdash filter on controller"() {
        given:
        Book.withNewSession {
            Publisher p = new Publisher(firstName: 'Some', lastName: 'Publisher').save(flush: true, failOnError: true)
            Author dm = new Author(firstName: 'Dave', lastName: 'Mark', favoriteGenre: FavoriteGenre.Reference, publisher: p).save(flush: true, failOnError: true)
            new Book(authors: [dm], title: 'Hello�how are you', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save(flush: true, failOnError: true)
        }

        when:
        def model
        params.filter = [op: [title: 'ILike'], title: '�']
        Book.withNewSession {
            controller.filter()
            model = controller.modelAndView.model
        }

        then:
        model.bookList.size() == 1
        model.bookCount == 1
        model.bookList.find { it.title == 'Hello�how are you' }
    }

    def "test filter by title text on controller"() {
        given:
        Book.withNewSession {
            Publisher p = new Publisher(firstName: 'Some', lastName: 'Publisher').save(flush: true, failOnError: true)
            Author dm = new Author(firstName: 'Dave', lastName: 'Mark', favoriteGenre: FavoriteGenre.Reference, publisher: p).save(flush: true, failOnError: true)
            new Book(authors: [dm], title: 'I like cheese', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save(flush: true, failOnError: true)
            new Book(authors: [dm], title: 'I like apples', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save(flush: true, failOnError: true)
        }

        when:
        def model
        params.filter = [op: [title: 'ILike'], title: 'like']
        Book.withNewSession {
            controller.filter()
            model = controller.modelAndView.model
        }

        then:
        model.bookList.size() == 2
        model.bookCount == 2
        model.bookList.find { it.title == 'I like cheese' }
    }
}
