package com.ekene.servicebackendfintech.transaction.api;

import com.ekene.servicebackendfintech.transaction.model.Transaction;
import com.ekene.servicebackendfintech.transaction.service.TransactionService;
import com.ekene.servicebackendfintech.utils.ApiResponse;
import com.ekene.servicebackendfintech.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/transaction/")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("statement")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getStatement(@RequestParam String userId, @RequestParam String from,
                                                                       @RequestParam String to, @RequestParam int size,
                                                                       @RequestParam int length){
        return transactionService.generateStatement(userId,
                DateUtil.convertStringToLocalDate(from), DateUtil.convertStringToLocalDate(to), size, length);
    }
}
