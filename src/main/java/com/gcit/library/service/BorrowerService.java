/**
 * 
 */
package com.gcit.library.service;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gcit.library.dao.BorrowerDao;
import com.gcit.library.dao.LoanDao;
import com.gcit.library.model.Borrower;
import com.gcit.library.model.Loan;

/**
 * @author gcit
 *
 */


@RestController
@CrossOrigin(origins="http://localhost:3000")
public class BorrowerService {
	
	@Autowired
	BorrowerDao bodao;
	
	@Autowired
	LoanDao ldao;
	
	@Transactional
	@RequestMapping(value="/validateCardNo/{cardNo}", method=RequestMethod.GET)
	public ResponseEntity<Object> validateCardNo(@PathVariable(value="cardNo") Integer cardNo) {
		try {
			List<Borrower> borrowers = bodao.getBorrowerByPK("select * from tbl_borrower where cardNo = ?", new Object[] {cardNo});
			if(borrowers.size() == 0) {
				return new ResponseEntity<Object>(false,HttpStatus.OK);
			}
			return new ResponseEntity<Object>(true,HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional
	@RequestMapping(value="/borrowers/{cardNo}", method=RequestMethod.GET)
	public ResponseEntity<Object> getBorrowHistory(@PathVariable(value="cardNo") Integer cardNo) {
		try {
			List<Loan> loans = ldao.getLoans("select book.bookId, branch.branchId, borrower.cardNo,book.title, borrower.name,branch.branchName,loan.dateOut,loan.dueDate,loan.dateIn from tbl_book book\n" + 
					"join tbl_book_loans loan on book.bookId = loan.bookId\n" + 
					"join tbl_library_branch branch on loan.branchId = branch.branchId\n" + 
					"join tbl_borrower borrower on loan.cardNo = borrower.cardNo\n" + 
					"where loan.cardNo = ?;",new Object[] {cardNo});
			return new ResponseEntity<Object>(loans,HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional
	@RequestMapping(value="/borrowers/{cardNo}", method=RequestMethod.PUT)
	public ResponseEntity<Object> returnBook(@PathVariable(value="cardNo") Integer cardNo,
			@RequestBody Loan loan) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(URI.create("/borrowers/"+cardNo));
			ldao.updateLoan("update tbl_book_loans set dateIn = ? where bookId = ? and branchId = ? and cardNo = ?", 
					new Object[] {loan.getDateIn(),loan.getBookId(),loan.getBranchId(),loan.getCardNo()});
			return new ResponseEntity<Object>(headers,HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional
	@RequestMapping(value="/borrowers/{cardNo}", method=RequestMethod.POST)
	public ResponseEntity<Object> checkOutBook(@PathVariable(value="cardNo") Integer cardNo,
			@RequestBody Loan loan) {
		try {
			ldao.updateLoan("update tbl_book_loans set dateIn = ? where bookId = ? and branchId = ? and cardNo = ?", 
					new Object[] {loan.getDateIn(),loan.getBookId(),loan.getBranchId(),loan.getCardNo()});
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(URI.create("/borrowers/"+cardNo));
			return new ResponseEntity<Object>(headers,HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
