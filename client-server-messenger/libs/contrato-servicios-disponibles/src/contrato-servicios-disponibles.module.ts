import { Module } from '@nestjs/common';
import { ContratoServiciosDisponiblesService } from './contrato-servicios-disponibles.service.js';

@Module({
  providers: [ContratoServiciosDisponiblesService],
  exports: [ContratoServiciosDisponiblesService],
})
export class ContratoServiciosDisponiblesModule {}
