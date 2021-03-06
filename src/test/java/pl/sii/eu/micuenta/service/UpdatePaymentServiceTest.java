package pl.sii.eu.micuenta.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import pl.sii.eu.micuenta.conf.AppConfig;
import pl.sii.eu.micuenta.conf.DataCreator;
import pl.sii.eu.micuenta.model.model_dto.CreditCard;
import pl.sii.eu.micuenta.model.model_entity.CreditCardEntity;
import pl.sii.eu.micuenta.model.model_entity.DebtEntity;
import pl.sii.eu.micuenta.model.model_entity.DebtorEntity;
import pl.sii.eu.micuenta.model.model_dto.form.PaymentConfirmation;
import pl.sii.eu.micuenta.model.model_dto.form.PaymentDeclaration;
import pl.sii.eu.micuenta.repository.AccountsRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@Transactional
@Rollback
@SpringBootTest
public class UpdatePaymentServiceTest {

    @Autowired
    DebtCalculatorService debtCalculatorService;
    @Autowired
    private UpdatePaymentService updatePaymentService;
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private DataCreator dataCreator;

    @Test
    @Sql(scripts = "/sql_scripts/initial_db_state.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql_scripts/clean_db.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldProperlyExtendsPaymentsSetsSizeAfterAddingPaymentWithChosenDebtId() {

        //given
        DebtorEntity debtorEntity = dataCreator.createDebtor();
        accountsRepository.save(debtorEntity);

        CreditCardEntity creditCardEntityOne = new CreditCardEntity("1234567890123456", "809", "Sylvanas", "Windrunner", "MasterCard", LocalDate.now());
        CreditCardEntity creditCardEntityTwo = new CreditCardEntity("0987654321098765", "312", "Anduin", "Wrynn", "VISA", LocalDate.now());
        PaymentDeclaration paymentDeclarationOne = new PaymentDeclaration(BigDecimal.valueOf(2043), "980-122-111", "PLWT/871422");
        PaymentDeclaration paymentDeclarationTwo = new PaymentDeclaration(BigDecimal.valueOf(3456), "980-122-111", "PLWT/871422");
        PaymentDeclaration paymentDeclarationThree = new PaymentDeclaration(BigDecimal.valueOf(234), "980-122-111", "PLWT/871422");
        PaymentConfirmation paymentConfirmationOne = new PaymentConfirmation(paymentDeclarationOne, "Horde", CreditCard.convertFromCreditCardEntity(creditCardEntityOne));
        PaymentConfirmation paymentConfirmationTwo = new PaymentConfirmation(paymentDeclarationTwo, "Alliance", CreditCard.convertFromCreditCardEntity(creditCardEntityTwo));
        PaymentConfirmation paymentConfirmationThree = new PaymentConfirmation(paymentDeclarationThree, "Alliance", CreditCard.convertFromCreditCardEntity(creditCardEntityTwo));

        //when
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationOne);
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationTwo);
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationThree);

        int expectedPaymentsSizeOfDebtOne = 3;
        int expectedPaymentsSizeOfDebtTwo = 3;

        //then
        int resultPaymentsSizeOfDebtOne = 0;
        int resultPaymentsSizeOfDebtTwo = 0;

        for (DebtEntity d : debtorEntity.getDebtEntities()) {
            if (d.getUuid().equals("PLWT/871422"))
                resultPaymentsSizeOfDebtOne = d.getPaymentEntities().size();
            if (d.getUuid().equals("ADWR/595501"))
                resultPaymentsSizeOfDebtTwo = d.getPaymentEntities().size();
        }

        assertThat(expectedPaymentsSizeOfDebtOne).isEqualTo(resultPaymentsSizeOfDebtOne);
        assertThat(expectedPaymentsSizeOfDebtTwo).isEqualTo(resultPaymentsSizeOfDebtTwo);
    }

    @Test
    @Sql(scripts = "/sql_scripts/initial_db_state.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql_scripts/clean_db.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldProperlyExtendsPaymentsSetsSizeAfterAddingPaymentWithoutChosenDebtId() {

        //given
        DebtorEntity debtorEntity = dataCreator.createDebtor();
        accountsRepository.save(debtorEntity);

        CreditCardEntity creditCardEntityOne = new CreditCardEntity("1234567890123456", "809", "Sylvanas", "Windrunner", "MasterCard", LocalDate.now());
        CreditCardEntity creditCardEntityTwo = new CreditCardEntity("0987654321098765", "312", "Anduin", "Wrynn", "VISA", LocalDate.now());
        PaymentDeclaration paymentDeclarationOne = new PaymentDeclaration(BigDecimal.valueOf(2043), "980-122-111", "");
        PaymentDeclaration paymentDeclarationTwo = new PaymentDeclaration(BigDecimal.valueOf(35456), "980-122-111", "");
        PaymentDeclaration paymentDeclarationThree = new PaymentDeclaration(BigDecimal.valueOf(234), "980-122-111", "");
        PaymentConfirmation paymentConfirmationOne = new PaymentConfirmation(paymentDeclarationOne, "Horde", CreditCard.convertFromCreditCardEntity(creditCardEntityOne));
        PaymentConfirmation paymentConfirmationTwo = new PaymentConfirmation(paymentDeclarationTwo, "Alliance", CreditCard.convertFromCreditCardEntity(creditCardEntityTwo));
        PaymentConfirmation paymentConfirmationThree = new PaymentConfirmation(paymentDeclarationThree, "Alliance", CreditCard.convertFromCreditCardEntity(creditCardEntityTwo));

        //when
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationOne);
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationTwo);
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationThree);

        int expectedPaymentsSizeOfDebtOne = 3;
        int expectedPaymentsSizeOfDebtTwo = 3;

        //then
        int resultPaymentsSizeOfDebtOne = 0;
        int resultPaymentsSizeOfDebtTwo = 0;

        for (DebtEntity d : debtorEntity.getDebtEntities()) {
            if (d.getUuid().equals("ADWR/595501"))
                resultPaymentsSizeOfDebtOne = d.getPaymentEntities().size();
            if (d.getUuid().equals("PLWT/871422"))
                resultPaymentsSizeOfDebtTwo = d.getPaymentEntities().size();
        }

        assertThat(expectedPaymentsSizeOfDebtOne).isEqualTo(resultPaymentsSizeOfDebtOne);
        assertThat(expectedPaymentsSizeOfDebtTwo).isEqualTo(resultPaymentsSizeOfDebtTwo);
    }

    @Test
    @Sql(scripts = "/sql_scripts/initial_db_state.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql_scripts/clean_db.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldProperlyUpdateSumOfPaymentsAmountAfterAddingPaymentWithChosenDebtId() {

        //given
        DebtorEntity debtorEntity = dataCreator.createDebtor();
        accountsRepository.save(debtorEntity);

        CreditCardEntity creditCardEntityOne = new CreditCardEntity("1234567890123456", "809", "Sylvanas", "Windrunner", "MasterCard", LocalDate.now());
        CreditCardEntity creditCardEntityTwo = new CreditCardEntity("0987654321098765", "312", "Anduin", "Wrynn", "VISA", LocalDate.now());
        CreditCardEntity creditCardEntityThree = new CreditCardEntity("1010101010101010", "312", "Darion", "Mograine", "VISA", LocalDate.now());
        PaymentDeclaration paymentDeclarationOne = new PaymentDeclaration(BigDecimal.valueOf(2043), "980-122-111", "PLWT/871422");
        PaymentDeclaration paymentDeclarationTwo = new PaymentDeclaration(BigDecimal.valueOf(35456), "980-122-111", "ADWR/595501");
        PaymentDeclaration paymentDeclarationThree = new PaymentDeclaration(BigDecimal.valueOf(234), "980-122-111", "CRTP/909088");
        PaymentConfirmation paymentConfirmationOne = new PaymentConfirmation(paymentDeclarationOne, "Horde", CreditCard.convertFromCreditCardEntity(creditCardEntityOne));
        PaymentConfirmation paymentConfirmationTwo = new PaymentConfirmation(paymentDeclarationTwo, "Alliance", CreditCard.convertFromCreditCardEntity(creditCardEntityTwo));
        PaymentConfirmation paymentConfirmationThree = new PaymentConfirmation(paymentDeclarationThree, "Ebon Blade", CreditCard.convertFromCreditCardEntity(creditCardEntityThree));

        //when
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationOne);
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationTwo);
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationThree);

        BigDecimal expectedPaymentsSizeOfDebtOne = BigDecimal.valueOf(3320).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtTwo = BigDecimal.valueOf(35000).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtThree = BigDecimal.valueOf(1434).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtFour = BigDecimal.valueOf(1400).setScale(2, RoundingMode.HALF_EVEN);

        //then
        BigDecimal resultPaymentsSizeOfDebtOne = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtTwo = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtThree = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtFour = BigDecimal.ZERO;

        for (DebtEntity d : debtorEntity.getDebtEntities()) {
            if (d.getUuid().equals("PLWT/871422"))
                resultPaymentsSizeOfDebtOne = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else if (d.getUuid().equals("ADWR/595501"))
                resultPaymentsSizeOfDebtTwo = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else if (d.getUuid().equals("CRTP/909088"))
                resultPaymentsSizeOfDebtThree = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else
                resultPaymentsSizeOfDebtFour = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
        }

        assertThat(expectedPaymentsSizeOfDebtOne).isEqualTo(resultPaymentsSizeOfDebtOne);
        assertThat(expectedPaymentsSizeOfDebtTwo).isEqualTo(resultPaymentsSizeOfDebtTwo);
        assertThat(expectedPaymentsSizeOfDebtThree).isEqualTo(resultPaymentsSizeOfDebtThree);
        assertThat(expectedPaymentsSizeOfDebtFour).isEqualTo(resultPaymentsSizeOfDebtFour);
    }

    @Test
    @Sql(scripts = "/sql_scripts/initial_db_state.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql_scripts/clean_db.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldProperlyUpdateSumOfPaymentsAmountAfterAddingPaymentWithoutChosenDebtId() {

        //given
        DebtorEntity debtorEntity = dataCreator.createDebtor();
        accountsRepository.save(debtorEntity);

        CreditCardEntity creditCardEntityOne = new CreditCardEntity("1234567890123456", "809", "Sylvanas", "Windrunner", "MasterCard", LocalDate.now());
        CreditCardEntity creditCardEntityTwo = new CreditCardEntity("0987654321098765", "312", "Anduin", "Wrynn", "VISA", LocalDate.now());
        CreditCardEntity creditCardEntityThree = new CreditCardEntity("1010101010101010", "312", "Darion", "Mograine", "VISA", LocalDate.now());
        PaymentDeclaration paymentDeclarationOne = new PaymentDeclaration(BigDecimal.valueOf(20250), "980-122-111", "");
        PaymentDeclaration paymentDeclarationTwo = new PaymentDeclaration(BigDecimal.valueOf(35456), "980-122-111", "");
        PaymentDeclaration paymentDeclarationThree = new PaymentDeclaration(BigDecimal.valueOf(2348), "980-122-111", "");
        PaymentConfirmation paymentConfirmationOne = new PaymentConfirmation(paymentDeclarationOne, "Horde", CreditCard.convertFromCreditCardEntity(creditCardEntityOne));
        PaymentConfirmation paymentConfirmationTwo = new PaymentConfirmation(paymentDeclarationTwo, "Alliance", CreditCard.convertFromCreditCardEntity(creditCardEntityTwo));
        PaymentConfirmation paymentConfirmationThree = new PaymentConfirmation(paymentDeclarationThree, "Ebon Blade", CreditCard.convertFromCreditCardEntity(creditCardEntityThree));

        //when
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationOne);
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationTwo);
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationThree);

        BigDecimal expectedPaymentsSizeOfDebtOne = BigDecimal.valueOf(4000).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtTwo = BigDecimal.valueOf(35000).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtThree = BigDecimal.valueOf(21075).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtFour = BigDecimal.valueOf(1400).setScale(2, RoundingMode.HALF_EVEN);

        //then
        BigDecimal resultPaymentsSizeOfDebtOne = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtTwo = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtThree = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtFour = BigDecimal.ZERO;

        for (DebtEntity d : debtorEntity.getDebtEntities()) {
            if (d.getUuid().equals("PLWT/871422"))
                resultPaymentsSizeOfDebtOne = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else if (d.getUuid().equals("ADWR/595501"))
                resultPaymentsSizeOfDebtTwo = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else if (d.getUuid().equals("CRTP/909088"))
                resultPaymentsSizeOfDebtThree = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else
                resultPaymentsSizeOfDebtFour = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
        }

        assertThat(expectedPaymentsSizeOfDebtOne).isEqualTo(resultPaymentsSizeOfDebtOne);
        assertThat(expectedPaymentsSizeOfDebtTwo).isEqualTo(resultPaymentsSizeOfDebtTwo);
        assertThat(expectedPaymentsSizeOfDebtThree).isEqualTo(resultPaymentsSizeOfDebtThree);
        assertThat(expectedPaymentsSizeOfDebtFour).isEqualTo(resultPaymentsSizeOfDebtFour);
    }

    @Test
    @Sql(scripts = "/sql_scripts/initial_db_state.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql_scripts/clean_db.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldPaidAllDebtsWithPaymentAmountBiggerThanSumOfAllDebtorsDebts() {

        //given
        DebtorEntity debtorEntity = dataCreator.createDebtor();
        accountsRepository.save(debtorEntity);

        CreditCardEntity creditCardEntityOne = new CreditCardEntity("1234567890123456", "111", "Arthas", "Menethil", "MasterCard", LocalDate.now());
        PaymentDeclaration paymentDeclarationOne = new PaymentDeclaration(BigDecimal.valueOf(175000), "980-122-111", "");
        PaymentConfirmation paymentConfirmationOne = new PaymentConfirmation(paymentDeclarationOne, "Scourge", CreditCard.convertFromCreditCardEntity(creditCardEntityOne));

        //when
        updatePaymentService.updateDebtsPaymentsBasedOnPaymentConfirmation(paymentConfirmationOne);

        BigDecimal expectedPaymentsSizeOfDebtOne = BigDecimal.valueOf(4000).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtTwo = BigDecimal.valueOf(35000).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtThree = BigDecimal.valueOf(50000).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal expectedPaymentsSizeOfDebtFour = BigDecimal.valueOf(60000).setScale(2, RoundingMode.HALF_EVEN);

        //then
        BigDecimal resultPaymentsSizeOfDebtOne = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtTwo = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtThree = BigDecimal.ZERO;
        BigDecimal resultPaymentsSizeOfDebtFour = BigDecimal.ZERO;

        for (DebtEntity d : debtorEntity.getDebtEntities()) {
            if (d.getUuid().equals("PLWT/871422"))
                resultPaymentsSizeOfDebtOne = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else if (d.getUuid().equals("ADWR/595501"))
                resultPaymentsSizeOfDebtTwo = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else if (d.getUuid().equals("CRTP/909088"))
                resultPaymentsSizeOfDebtThree = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
            else
                resultPaymentsSizeOfDebtFour = debtCalculatorService.getSumOfPayments(d).setScale(2, RoundingMode.HALF_EVEN);
        }

        assertThat(expectedPaymentsSizeOfDebtOne).isEqualTo(resultPaymentsSizeOfDebtOne);
        assertThat(expectedPaymentsSizeOfDebtTwo).isEqualTo(resultPaymentsSizeOfDebtTwo);
        assertThat(expectedPaymentsSizeOfDebtThree).isEqualTo(resultPaymentsSizeOfDebtThree);
        assertThat(expectedPaymentsSizeOfDebtFour).isEqualTo(resultPaymentsSizeOfDebtFour);
    }
}