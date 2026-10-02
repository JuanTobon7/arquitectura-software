import { Module } from '@nestjs/common';
import { ConexionesClientesService } from './conexiones-clientes.service.js';

@Module({
  providers: [ConexionesClientesService]
})
export class ConexionesClientesModule {}
