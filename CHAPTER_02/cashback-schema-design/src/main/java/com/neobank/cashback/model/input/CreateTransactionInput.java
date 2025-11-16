package com.neobank.cashback.model.input;

import com.neobank.cashback.model.TransactionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Input para crear una nueva transacción.
 * 
 * SECCIÓN 2.2: Diferencia entre INPUT TYPES y OUTPUT TYPES
 * 
 * ¿Por qué un Input separado y no reusar Transaction?
 * 
 * 1. FLEXIBILIDAD:
 *    - Input puede tener campos diferentes al output
 *    - Ej: Input no tiene 'id' (se genera en servidor)
 *    - Ej: Input no tiene 'status' (siempre empieza PENDING)
 * 
 * 2. VALIDACIÓN:
 *    - Inputs pueden tener validaciones específicas
 *    - Required fields diferentes entre create/update
 * 
 * 3. EVOLUCIÓN:
 *    - Puedes cambiar Input sin afectar Output (y viceversa)
 *    - Backward compatibility más fácil
 * 
 * REGLA GraphQL:
 * Input types NO pueden tener campos de tipo Object, solo escalares/enums/otros inputs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionInput {
    private String userId;
    private Double amount;
    private TransactionCategory category;
    private String merchantName;
    private String description;         // Opcional
    private LocalDateTime transactionDate;  // Opcional (default: now)
}
